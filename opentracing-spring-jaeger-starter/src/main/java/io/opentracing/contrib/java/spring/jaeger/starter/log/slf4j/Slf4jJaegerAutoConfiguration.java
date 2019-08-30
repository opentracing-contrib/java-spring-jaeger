/**
 * Copyright 2018-2019 The OpenTracing Authors
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
package io.opentracing.contrib.java.spring.jaeger.starter.log.slf4j;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerAutoConfiguration;
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerConfigurationProperties;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:taylor.tian@ericsson.com">Taylor Tian</a>
 */
@Configuration
@ConditionalOnClass({JaegerTracer.class,MDC.class})
@ConditionalOnProperty(
        value = {"opentracing.jaeger.log.slf4j.enabled"},
        havingValue = "true",
        matchIfMissing = true
)
@AutoConfigureAfter({JaegerAutoConfiguration.class})
@EnableConfigurationProperties(JaegerConfigurationProperties.class)
public class Slf4jJaegerAutoConfiguration {
  @Bean
  public TracerBuilderCustomizer logTracerCustomizer(JaegerConfigurationProperties properties) {
    TracerBuilderCustomizer customizer = new Slf4jTracerBuilderCustomizer(properties);

    return customizer;
  }
}
