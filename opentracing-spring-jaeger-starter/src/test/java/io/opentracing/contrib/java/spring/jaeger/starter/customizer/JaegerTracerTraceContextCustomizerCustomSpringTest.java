/**
 * Copyright 2018-2020 The OpenTracing Authors
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

package io.opentracing.contrib.java.spring.jaeger.starter.customizer;

import static org.assertj.core.api.Assertions.assertThat;

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.java.spring.jaeger.starter.AbstractTracerSpringTest;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;


@TestPropertySource(
    properties = {
        "spring.main.banner-mode=off",
        "opentracing.jaeger.enable-w3c-propagation=true"
    }
)
@Import(JaegerTracerTraceContextCustomizerCustomSpringTest.TestConfiguration.class)
public class JaegerTracerTraceContextCustomizerCustomSpringTest extends AbstractTracerSpringTest {

  @Autowired
  private Tracer tracer;

  public static class TestConfiguration {
    @Bean
    public TracerBuilderCustomizer myCustomizer() {
      // Noop
      return builder -> {
      };
    }
  }

  @Test
  public void testCustomizersHttpHeadersShouldContainTraceContext() {
    TextMap textMap = createTextMap();

    JaegerSpanContext context = (JaegerSpanContext) tracer.extract(Format.Builtin.HTTP_HEADERS, textMap);

    assertOnTraceContextHeaders(context);
  }

  @Test
  public void testCustomizersTextMapShouldContainTraceContext() {
    TextMap textMap = createTextMap();

    JaegerSpanContext context = (JaegerSpanContext) tracer.extract(Format.Builtin.TEXT_MAP, textMap);

    assertOnTraceContextHeaders(context);
  }

  private TextMapAdapter createTextMap() {
    Map<String, String> carrier = new HashMap<>();
    carrier.put("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");
    carrier.put("tracestate", "congo=t61rcWkgMzE");

    return new TextMapAdapter(carrier);
  }

  private void assertOnTraceContextHeaders(JaegerSpanContext context) {
    // Note: This test ensures that W3C Trace Context codec actually works
    // If it would not, values would never be extracted from B3 headers and context will be null
    assertThat(context).isNotNull();
    assertThat(context.getTraceId()).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
    assertThat(context.getSpanId()).isEqualTo(67667974448284343L);
  }
}
