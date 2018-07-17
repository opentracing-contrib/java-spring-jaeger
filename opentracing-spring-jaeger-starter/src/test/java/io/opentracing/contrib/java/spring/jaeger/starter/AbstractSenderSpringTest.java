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

import static org.assertj.core.api.Assertions.assertThat;

import io.jaegertracing.internal.reporters.CompositeReporter;
import io.jaegertracing.internal.reporters.RemoteReporter;
import org.assertj.core.api.Condition;

public abstract class AbstractSenderSpringTest extends AbstractTracerSpringTest {

  protected void assertSenderClass(Class senderClass) {
    assertThat(getTracer())
        .extracting("reporter")
        .extracting("class")
        .containsExactly(CompositeReporter.class);

    assertThat(getTracer())
        .extracting("reporter")
        .flatExtracting("reporters")
        .filteredOn(new Condition<Object>() {
          @Override
          public boolean matches(Object value) {
            return value.getClass().equals(RemoteReporter.class);
          }
        })
        .extracting("sender")
        .extracting("class")
        .containsExactly(senderClass);
  }
}
