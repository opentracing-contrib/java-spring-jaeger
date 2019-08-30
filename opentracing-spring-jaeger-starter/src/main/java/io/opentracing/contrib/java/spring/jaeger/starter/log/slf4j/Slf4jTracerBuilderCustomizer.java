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
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerConfigurationProperties;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import io.opentracing.util.ThreadLocalScopeManager;

/**
 * @author <a href="mailto:taylor.tian@ericsson.com">Taylor Tian</a>
 */
public class Slf4jTracerBuilderCustomizer implements TracerBuilderCustomizer {
  private JaegerConfigurationProperties properties;

  public Slf4jTracerBuilderCustomizer(JaegerConfigurationProperties properties) {
    this.properties = properties;
  }

  @Override
  public void customize(JaegerTracer.Builder builder) {
    builder.withScopeManager(new ScopeManagerSlf4jDecorator(new ThreadLocalScopeManager(), properties));
  }
}