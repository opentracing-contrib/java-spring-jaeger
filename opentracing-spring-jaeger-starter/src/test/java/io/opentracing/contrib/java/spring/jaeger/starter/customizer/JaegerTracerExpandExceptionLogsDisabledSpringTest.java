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

package io.opentracing.contrib.java.spring.jaeger.starter.customizer;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentracing.contrib.java.spring.jaeger.starter.AbstractTracerSpringTest;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.ExpandExceptionLogsTracerBuilderCustomizer;
import java.util.List;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
    properties = {
        "spring.main.banner-mode=off",
        "opentracing.jaeger.expand-exception-logs=false"
    }
)
public class JaegerTracerExpandExceptionLogsDisabledSpringTest extends AbstractTracerSpringTest {

  @Autowired(required = false)
  private List<TracerBuilderCustomizer> customizers;

  @Test
  public void testCustomizersShouldContainExpandLogsCustomizer() {
    if (customizers == null) {
      return;
    }

    assertThat(customizers)
        .extracting("class").doesNotContain(ExpandExceptionLogsTracerBuilderCustomizer.class);
  }
}
