/**
 * Copyright 2018-2021 The OpenTracing Authors
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

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import io.jaegertracing.internal.reporters.CompositeReporter;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.spi.Reporter;
import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;

public abstract class AbstractSenderSpringTest extends AbstractTracerSpringTest {

  protected void assertSenderClass(Class senderClass) {
    assertThat(getTracer())
            .extracting("reporter")
            .extracting("class", as(InstanceOfAssertFactories.CLASS))
            .isEqualTo(CompositeReporter.class);

    assertThat(getTracer())
            .extracting("reporter").isInstanceOfSatisfying(CompositeReporter.class, c -> {
              assertThat(c)
                  .extracting("reporters", as(InstanceOfAssertFactories.list(Reporter.class)))
                  .filteredOn(new Condition<Object>() {
                    @Override
                    public boolean matches(Object value) {
                        return value.getClass().equals(RemoteReporter.class);
                    }
                    }).allSatisfy(rr -> {
                      assertThat(rr)
                        .extracting("sender")
                        .extracting("class", as(InstanceOfAssertFactories.CLASS))
                        .isEqualTo(senderClass);
                    });
            });
  }
}
