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

import io.jaegertracing.internal.JaegerTracer.Builder;
import io.opentracing.contrib.java.spring.jaeger.starter.AbstractTracerSpringTest;
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerAutoConfiguration;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.B3CodecTracerBuilderCustomizer;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.ExpandExceptionLogsTracerBuilderCustomizer;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.HigherBitTracerBuilderCustomizer;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {
    MultipleCustomizersEnabledSpringTest.MockTracerConfiguration.class,
    JaegerAutoConfiguration.class
})
@TestPropertySource(
    properties = {
        "spring.main.banner-mode=off",
        "opentracing.jaeger.expand-exception-logs=true",
        "opentracing.jaeger.enable-b3-propagation=true",
        "opentracing.jaeger.enable-128-bit-traces=true"
    }
)
public class MultipleCustomizersEnabledSpringTest extends AbstractTracerSpringTest {

  @Autowired
  private List<TracerBuilderCustomizer> customizers;

  @Test
  public void testCustomizersShouldContainB3Customizer() {
    assertThat(customizers)
        .isNotEmpty()
        .extracting("class")
        .containsExactlyInAnyOrder(
            ExpandExceptionLogsTracerBuilderCustomizer.class,
            B3CodecTracerBuilderCustomizer.class,
            HigherBitTracerBuilderCustomizer.class,
            MockTracerBuilderCustomizer.class);
  }

  @Configuration
  public static class MockTracerConfiguration {

    @Bean
    public TracerBuilderCustomizer mockCustomizer() {
      return new MockTracerBuilderCustomizer();
    }
  }

  public static class MockTracerBuilderCustomizer implements TracerBuilderCustomizer {

    @Override
    public void customize(Builder builder) {
    }
  }
}
