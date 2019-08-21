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

package io.opentracing.contrib.java.spring.jaeger.starter.basic;

import io.opentracing.contrib.java.spring.jaeger.starter.AbstractTracerSpringTest;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Java6Assertions.assertThat;

@TestPropertySource(
    properties = {
        "spring.main.banner-mode=off",
        "opentracing.jaeger.enabled=true",
        "jaeger.service-name=foo",
        "spring.application.name=bar"
    }
)
public class JaegerTracerServiceNameSetWithoutOpentracingPrefixTest extends AbstractTracerSpringTest {

  @Test
  public void testNameIsAsExpected() {
    assertThat(tracer).isNotNull();
    assertThat(tracer).isInstanceOf(io.jaegertracing.internal.JaegerTracer.class);

    // 'jaeger.service-name' should take priority over 'spring.application.name'
    assertThat((getTracer()).getServiceName()).isEqualTo("foo");
  }
}
