/**
 * Copyright 2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.java.spring.jaeger.starter;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.metrics.Metrics;
import io.jaegertracing.internal.metrics.NoopMetricsFactory;
import io.jaegertracing.internal.reporters.CompositeReporter;
import io.jaegertracing.internal.reporters.LoggingReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.internal.samplers.HttpSamplingManager;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.jaegertracing.internal.samplers.RateLimitingSampler;
import io.jaegertracing.internal.samplers.RemoteControlledSampler;
import io.jaegertracing.spi.MetricsFactory;
import io.jaegertracing.spi.Reporter;
import io.jaegertracing.spi.Sampler;
import io.jaegertracing.spi.Sender;
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerConfigurationProperties.RemoteReporter;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.B3CodecTracerBuilderCustomizer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Configuration
@ConditionalOnClass(io.jaegertracing.internal.JaegerTracer.class)
@ConditionalOnMissingBean(io.opentracing.Tracer.class)
@ConditionalOnProperty(value = "opentracing.jaeger.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration.class)
@EnableConfigurationProperties(JaegerConfigurationProperties.class)
public class JaegerAutoConfiguration {

  @Autowired(required = false)
  private List<TracerBuilderCustomizer> tracerCustomizers = Collections.emptyList();

  @Value("${spring.application.name:unknown-spring-boot}")
  private String serviceName;

  @Bean
  public io.opentracing.Tracer tracer(Sampler sampler,
                                      Reporter reporter) {

    final JaegerTracer.Builder builder =
        new JaegerTracer.Builder(serviceName)
            .withReporter(reporter)
            .withSampler(sampler);

    tracerCustomizers.forEach(c -> c.customize(builder));

    return builder.build();
  }

  @ConditionalOnMissingBean
  @Bean
  public Reporter reporter(JaegerConfigurationProperties properties,
      Metrics metrics,
      @Autowired(required = false) ReporterAppender reporterAppender) {

    List<Reporter> reporters = new LinkedList<>();
    RemoteReporter remoteReporter = properties.getRemoteReporter();

    JaegerConfigurationProperties.HttpSender httpSender = properties.getHttpSender();
    if (!StringUtils.isEmpty(httpSender.getUrl())) {
      reporters.add(getHttpReporter(metrics, remoteReporter, httpSender));
    } else {
      reporters.add(getUdpReporter(metrics, remoteReporter, properties.getUdpSender()));
    }

    if (properties.isLogSpans()) {
      reporters.add(new LoggingReporter());
    }

    if (reporterAppender != null) {
      reporterAppender.append(reporters);
    }

    return new CompositeReporter(reporters.toArray(new Reporter[reporters.size()]));
  }

  private Reporter getUdpReporter(Metrics metrics,
      RemoteReporter remoteReporter,
      JaegerConfigurationProperties.UdpSender udpSenderProperties) {
    io.jaegertracing.thrift.internal.senders.UdpSender udpSender =
        new io.jaegertracing.thrift.internal.senders.UdpSender(
          udpSenderProperties.getHost(), udpSenderProperties.getPort(),
          udpSenderProperties.getMaxPacketSize());

    return createReporter(metrics, remoteReporter, udpSender);
  }

  private Reporter getHttpReporter(Metrics metrics,
      RemoteReporter remoteReporter,
      JaegerConfigurationProperties.HttpSender httpSenderProperties) {
    io.jaegertracing.thrift.internal.senders.HttpSender.Builder builder =
        new io.jaegertracing.thrift.internal.senders.HttpSender.Builder(httpSenderProperties.getUrl());
    if (httpSenderProperties.getMaxPayload() != null) {
      builder = builder.withMaxPacketSize(httpSenderProperties.getMaxPayload());
    }
    if (!StringUtils.isEmpty(httpSenderProperties.getUsername())
        && !StringUtils.isEmpty(httpSenderProperties.getPassword())) {
      builder.withAuth(httpSenderProperties.getUsername(), httpSenderProperties.getPassword());
    } else if (!StringUtils.isEmpty(httpSenderProperties.getAuthToken())) {
      builder.withAuth(httpSenderProperties.getAuthToken());
    }

    return createReporter(metrics, remoteReporter, builder.build());
  }

  private Reporter createReporter(Metrics metrics,
      RemoteReporter remoteReporter, Sender udpSender) {
    io.jaegertracing.internal.reporters.RemoteReporter.Builder builder =
        new io.jaegertracing.internal.reporters.RemoteReporter.Builder()
            .withSender(udpSender)
            .withMetrics(metrics);

    if (remoteReporter.getFlushInterval() != null) {
      builder.withFlushInterval(remoteReporter.getFlushInterval());
    }
    if (remoteReporter.getMaxQueueSize() != null) {
      builder.withMaxQueueSize(remoteReporter.getMaxQueueSize());
    }

    return builder.build();
  }

  @ConditionalOnMissingBean
  @Bean
  public Metrics reporterMetrics(MetricsFactory metricsFactory) {
    return new Metrics(metricsFactory);
  }

  @ConditionalOnMissingBean
  @Bean
  public MetricsFactory metricsFactory() {
    return new NoopMetricsFactory();
  }

  /**
   * Decide on what Sampler to use based on the various configuration options in
   * JaegerConfigurationProperties Fallback to ConstSampler(true) when no Sampler is configured
   */
  @ConditionalOnMissingBean
  @Bean
  public Sampler sampler(JaegerConfigurationProperties properties, Metrics metrics) {
    if (properties.getConstSampler().getDecision() != null) {
      return new ConstSampler(properties.getConstSampler().getDecision());
    }

    if (properties.getProbabilisticSampler().getSamplingRate() != null) {
      return new ProbabilisticSampler(properties.getProbabilisticSampler().getSamplingRate());
    }

    if (properties.getRateLimitingSampler().getMaxTracesPerSecond() != null) {
      return new RateLimitingSampler(properties.getRateLimitingSampler().getMaxTracesPerSecond());
    }

    if (!StringUtils.isEmpty(properties.getRemoteControlledSampler().getHostPort())) {
      JaegerConfigurationProperties.RemoteControlledSampler samplerProperties
          = properties.getRemoteControlledSampler();

      return new RemoteControlledSampler.Builder(serviceName)
          .withSamplingManager(new HttpSamplingManager(samplerProperties.getHostPort()))
          .withInitialSampler(
              new ProbabilisticSampler(samplerProperties.getSamplingRate()))
          .withMetrics(metrics)
          .build();
    }

    //fallback to sampling every trace
    return new ConstSampler(true);
  }

  @Configuration
  @ConditionalOnProperty(value = "opentracing.jaeger.enable-b3-propagation")
  public static class B3CodecConfiguration {

    @Bean
    public TracerBuilderCustomizer b3CodecJaegerTracerCustomizer() {
      return new B3CodecTracerBuilderCustomizer();
    }
  }
}
