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

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;


/**
 * @author <a href="mailto:taylor.tian@ericsson.com">Taylor Tian</a>
 */
public class ScopeManagerSlf4jDecorator implements ScopeManager {
  private static final String B3_TRACE_ID = "X-B3-TraceId";
  private static final String B3_SPAN_ID = "X-B3-SpanId";
  private static final String B3_SAMPLED = "X-B3-Sampled";
  private static final String B3_EXPORT = "X-Span-Export";
  private static final String OPENTRACING_TRACE_ID = "ToTraceId";
  private static final String OPENTRACING_SPAN_ID = "ToSpanId";
  private static final String SAMPLED = "Sampled";

  private ScopeManager scopeManager;
  private JaegerConfigurationProperties properties;

  public ScopeManagerSlf4jDecorator(ScopeManager scopeManager, JaegerConfigurationProperties properties) {
    this.scopeManager = scopeManager;
    this.properties = properties;
  }

  @Override
  public Scope activate(Span span) {
    Map<String, String> currentContext = addSpanToMDC(span);
    Scope scope = scopeManager.activate(span);

    return new ScopeSlf4jDecorator(scope, currentContext);
  }

  @Override
  @Deprecated
  public Scope activate(Span span, boolean finishSpanOnClose) {
    Map<String, String> currentContext = addSpanToMDC(span);
    Scope scope = scopeManager.activate(span, finishSpanOnClose);

    return new ScopeSlf4jDecorator(scope, currentContext);
  }

  @Override
  @Deprecated
  public Scope active() {
    return scopeManager.active();
  }

  @Override
  public Span activeSpan() {
    return scopeManager.activeSpan();
  }

  private Map<String, String> addSpanToMDC(Span span) {
    JaegerSpanContext context = (JaegerSpanContext) span.context();
    Map<String, String> currentContext = new HashMap<>();
    currentContext.put(OPENTRACING_TRACE_ID, context.toTraceId());
    currentContext.put(OPENTRACING_SPAN_ID, context.toSpanId());
    currentContext.put(SAMPLED, String.valueOf(context.isSampled()));

    if (properties.isEnableB3Propagation()) {
      currentContext.put(B3_TRACE_ID, context.toTraceId());
      currentContext.put(B3_SPAN_ID, context.toSpanId());
      currentContext.put(B3_SAMPLED, String.valueOf(context.isSampled()));
      currentContext.put(B3_EXPORT, String.valueOf(context.isSampled()));
    }

    for (Map.Entry<String, String> entry : currentContext.entrySet()) {
      MDC.put(entry.getKey(), entry.getValue());
    }

    return currentContext;
  }
}
