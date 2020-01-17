[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

## Dependencies

The `opentracing-spring-jaeger-starter` simply contains the code needed to provide a Jaeger implementation of the OpenTracing's `io.opentracing.Tracer`
interface.

For a project to be able to actually instrument a Spring stack, one or more of the purpose built starters (like `io.opentracing.contrib:opentracing-spring-web-starter` or `io.opentracing.contrib:opentracing-spring-cloud-starter`)  
would also have to be included in the POM.

The `opentracing-spring-jaeger-web-starter` starter is convenience starter that includes both `opentracing-spring-jaeger-starter` and `opentracing-spring-web-starter`
This means that by including it, simple web Spring Boot microservices include all the necessary dependencies to instrument Web requests / responses and send traces to Jaeger.

The `opentracing-spring-jaeger-cloud-starter` starter is convenience starter that includes both `opentracing-spring-jaeger-starter` and `opentracing-spring-cloud-starter`
This means that by including it, all parts of the Spring Cloud stack supported by Opentracing will be instrumented   


## Library versions

Versions 1.x.y of the library are meant to target Spring Boot 2.x while versions 0.x.y are meant to be used with Spring Boot 1.5

## Configuration

```xml
<dependency>
  <groupId>io.opentracing.contrib</groupId>
  <artifactId>opentracing-spring-jaeger-web-starter</artifactId>
</dependency>
```

or

```xml
<dependency>
  <groupId>io.opentracing.contrib</groupId>
  <artifactId>opentracing-spring-jaeger-cloud-starter</artifactId>
</dependency>
```

Either dependency will ensure that Spring Boot will auto configure a Jaeger implementation of OpenTracing's `Tracer` when the application starts.

If no settings are changed, spans will be reported to the UDP port `6831` of `localhost`.
The simplest way to change this behavior is to set the following properties:

```
opentracing.jaeger.udp-sender.host=jaegerhost
opentracing.jaeger.udp-sender.port=portNumber
```

for the UDP sender, or use an HTTP sender by setting the following property:
 
`opentracing.jaeger.http-sender.url = http://jaegerhost:portNumber/api/traces` 
   

## Configuration options

All the available configuration options can be seen in [JaegerConfigurationProperties](opentracing-spring-jaeger-starter/src/main/java/io/opentracing/contrib/java/spring/jaeger/starter/JaegerAutoConfiguration.java).
The prefix to be used for these properties is `opentracing.jaeger`.
Furthermore, the service name is configured via the standard Spring Cloud `spring.application.name` property.

Beware to use the correct syntax for properties that are camel-case in `JaegerConfigurationProperties`.

* For properties / yaml files use `-`. For example `opentracing.jaeger.log-spans=true`
* For environment variables use `_`. For example `OPENTRACING_JAEGER_LOG_SPANS` 

## Defaults

If no configuration options are changed and the user does not manually provide any of the beans that the 
auto-configuration process provides, the following defaults are used:

* `unknown-spring-boot` Will be used as the service-name if no value has been specified to the property `spring.application.name` or `opentracing.jaeger.service-name` (which has the highest priority). 
* `CompositeReporter` is provided which contains the following delegates:
  - `LoggingReporter` for reporting spans to the console
  - `RemoteReporter` that contains a `UdpSender` that sends spans to `localhost:6831` 
* `ConstSampler` with the value of `true`. This means that every trace will be sampled
* `NoopMetricsFactory` is used - effectively meaning that no metrics will be collected

## Senders

Configuring senders is as simple as setting a couple necessary properties

### HTTP Sender

`opentracing.jaeger.http-sender.url = http://jaegerhost:portNumber/api/traces`

It's possible to configure authentication on the HTTP sender by specifying an username and password:

`opentracing.jaeger.http-sender.username = username`
`opentracing.jaeger.http-sender.password = password`

Or by specifying a bearer token:

`opentracing.jaeger.http-sender.authtoken = token`
 

Note that when an HTTP Sender is defined, the UDP sender is not used, even if it has been configured

### UDP Sender

`opentracing.jaeger.udp-sender.host=jaegerhost`
`opentracing.jaeger.udp-sender.port=portNumber`

## Common cases

### Set service name 

Set `spring.application.name` to the desired name

### Log Spans

Be default spans are logged to the console. This can be disabled by setting:

`opentracing.jaeger.log-spans = false`

### Additional reporters

By defining a bean of type `ReporterAppender`, the code has the chance to add any Reporter without 
having to forgo what the auto-configuration provides  

### Sampling

* Const sampler

  `opentracing.jaeger.const-sampler.decision = true | false` 

* Probabilistic sampler

  `opentracing.jaeger.probabilistic-sampler.sampling-rate = value` 
  
  Where `value` is between `0.0` (no sampling) and `1.0` (sampling of every request)

* Rate-limiting sampler

  `opentracing.jaeger.rate-limiting-sampler.max-traces-per-second = value` 
  
  Configures that traces are sampled with a certain constant rate. For example, when sampler.param=2.0 it will sample requests with the rate of 2 traces per second.
  
  
The samplers above are mutually exclusive.

A custom sampler could of course be provided by declaring a bean of type `io.jaegertracing.samplers.Sampler`

### Propagate headers in B3 format (for compatibility with Zipkin collectors)

`opentracing.jaeger.enable-b3-propagation = true`

## Advanced cases

### Manual bean provisioning

Any of the following beans can be provided by the application (by adding configuring them as bean with `@Bean` for example)
and will be used to by the Tracer instead of the auto-configured beans.

* `io.jaegertracing.samplers.Sampler`
* `io.jaegertracing.metrics.MetricsFactory`  

### io.jaegertracing.Tracer.Builder customization

If arbitrary customizations need to be performed on `Tracer.Builder` but you don't want to forgo the rest of the auto-configuration
features, `TracerBuilderCustomizer` comes in handy. It allows the developer to invoke any method of `Tracer.Builder` (with the exception of `build`)
before the auto-configuration code invokes the `build` method.
Examples of this type of customization can be seen in the `B3CodecTracerBuilderCustomizer` and `ExpandExceptionLogsTracerBuilderCustomizer` classes. 

## Caution

### Beware of the default sampler in production

In a high traffic environment, the default sampler that is configured is very unsafe since it samples every request.
It is therefore highly recommended to explicitly configure on of the other options in a production environment

## Development
Maven checkstyle plugin is used to maintain consistent code style based on [Google Style Guides](https://github.com/google/styleguide)

```shell
./mvnw clean install
```

## Tips and tricks

### Completely disable tracing

There are times when it might be desirable to completely disable tracing (for example in a testing environment).
Due to the multiple (auto)configurations that come into play, this is not as simple as setting `opentracing.jaeger.enabled` to `false`.

When one of the starters of this project is included, then `io.opentracing.contrib:opentracing-spring-tracer-configuration-starter` is also included since it performs some necessary plumbing.
However, when `opentracing.jaeger.enabled` is set to `false`, then the aforementioned dependency provides a default `Tracer` implementation that needs the `JAEGER_SERVICE_NAME` environment variable (see [this](https://github.com/jaegertracing/jaeger-client-java/blob/master/jaeger-core/README.md)).

One simple way around this would be to do the add the following Spring configuration:

```java
@ConditionalOnProperty(value = "opentracing.jaeger.enabled", havingValue = "false", matchIfMissing = false)
@Configuration
public class MyTracerConfiguration {

    @Bean
    public io.opentracing.Tracer jaegerTracer() {
         final Reporter reporter = new InMemoryReporter();
         final Sampler sampler = new ConstSampler(false);
         return new JaegerTracer.Builder("untraced-service")
                    .withReporter(reporter)
                    .withSampler(sampler)
                    .build();
        }
    }

}
```

In the code above we are activating a `io.opentracing.Tracer` iff `opentracing.jaeger.enabled` is set to `false`. This tracer
is necessary to keep the various Spring configurations happy but has been configured to not sample any requests, therefore
effectively disabling tracing.

### Trace id not being forwarded

In some cases it might be necessary to explicitely expose the Feign client in the Spring configuration. This can be done easily by adding the following into one of your configuration classes:

```
@Bean
public Client feignClient() {
    return new Client.Default(null, null);
}
```

## Release
Follow instructions in [RELEASE](RELEASE.md)

   [ci-img]: https://travis-ci.org/opentracing-contrib/java-spring-jaeger.svg?branch=master
   [ci]: https://travis-ci.org/opentracing-contrib/java-spring-jaeger
   [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-spring-jaeger-starter.svg?maxAge=3600
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-spring-jaeger-starter

## License

[Apache 2.0 License](./LICENSE).
