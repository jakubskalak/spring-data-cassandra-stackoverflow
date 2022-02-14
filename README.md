## Description
On September 2021, my production service struggled with **StackOverflowError** during massive batch job, which task was to go through all records of table and do some computation.
It was using Spring Boot 2.5.4, Spring Data Cassandra 3.2.4, Cassandra Java Driver 4.11.3 and Java 11.0.2.

But problem exist also with newer versions, so it is not related to specific version of Spring Data Cassandra, Cassandra Driver or Java.

Decided to create simple service reproducing this issue, which hopefully may help with understanding the problem.

When trying to reproduce this issue on my local instance I determined that significant factor is  **spring.data.cassandra.request.page-size**. Lower value - quicker StackOverflowError.

And of course JVM stack size, which can be tuned using `-Xss` option - my production service used default value.

So for reproducing it quicker locally you can juggle them by changing under:
   - [application.yml](/src/main/resources/application.yml),
   - via environment variable **CASSANDRA_PAGE_SIZE**,
   - by setting `-Xss` argument e.g `-Xss200k`

Otherwise, you will need lots of records in table

## Service startup
1. From the **root** of the project go to [scripts](./scripts) directory

``` 
cd scripts/
```

2. Start **Cassandra** and create example schema
```
docker-compose up
```

3. Build and start service:
   - You can use predefined run configuration for Intellij [SpringDataCassandraStackoverflowApplication.xml](./.run/SpringDataCassandraStackoverflowApplication.run.xml)
   - or just build with Maven

## Reproducing the issue
1. At the beginning fill in the database with some data. You can do it using prepared endpoints.
Use **amount** parameter to specify records count. Suggested value is around 30-40k.
```
POST http://localhost:8080/errors/many/{{amount}}
```

2. Run job which will make page request going through all records.
```
POST http://localhost:8080/errors/batch
```

3. Having **root** log level **DEBUG**, you will see stacktrace:
```
2022-02-06 00:08:06.822 DEBUG 6230 --- [        s0-io-4] reactor.core.publisher.Operators         : Duplicate Subscription has been detected

java.lang.IllegalStateException: Spec. Rule 2.12 - Subscriber.onSubscribe MUST NOT be called more than once (based on object equality)
	at reactor.core.Exceptions.duplicateOnSubscribeException(Exceptions.java:180) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.Operators.reportSubscriptionSet(Operators.java:1084) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.Operators.setOnce(Operators.java:1189) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.onSubscribeInner(MonoFlatMapMany.java:146) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyInner.onSubscribe(MonoFlatMapMany.java:245) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.Operators.reportThrowInSubscribe(Operators.java:226) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.Flux.subscribe(Flux.java:8472) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.onNext(MonoFlatMapMany.java:195) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1816) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.NextProcessor.tryEmitValue(NextProcessor.java:301) ~[reactor-core-3.4.14.jar:3.4.14]
	at reactor.core.publisher.NextProcessor.onNext(NextProcessor.java:241) ~[reactor-core-3.4.14.jar:3.4.14]
	at org.springframework.data.cassandra.core.cql.session.DefaultBridgedReactiveSession$DefaultReactiveResultSet.lambda$fetchMore$2(DefaultBridgedReactiveSession.java:306) ~[spring-data-cassandra-3.3.1.jar:3.3.1]
	at java.base/java.util.concurrent.CompletableFuture.uniWhenComplete(CompletableFuture.java:863) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture$UniWhenComplete.tryFire(CompletableFuture.java:841) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:510) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:2147) ~[na:na]
	at com.datastax.oss.driver.internal.core.cql.CqlRequestHandler.setFinalResult(CqlRequestHandler.java:321) ~[java-driver-core-4.13.0.jar:na]
	at com.datastax.oss.driver.internal.core.cql.CqlRequestHandler.access$1500(CqlRequestHandler.java:94) ~[java-driver-core-4.13.0.jar:na]
	at com.datastax.oss.driver.internal.core.cql.CqlRequestHandler$NodeResponseCallback.onResponse(CqlRequestHandler.java:652) ~[java-driver-core-4.13.0.jar:na]
	at com.datastax.oss.driver.internal.core.channel.InFlightHandler.channelRead(InFlightHandler.java:257) ~[java-driver-core-4.13.0.jar:na]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.handler.timeout.IdleStateHandler.channelRead(IdleStateHandler.java:286) ~[netty-handler-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:327) ~[netty-codec-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:299) ~[netty-codec-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:722) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:658) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:584) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:496) ~[netty-transport-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:986) ~[netty-common-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.73.Final.jar:4.1.73.Final]
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.73.Final.jar:4.1.73.Final]
	at java.base/java.lang.Thread.run(Thread.java:833) ~[na:na]
```
4. Going through above stacktrace we can setup breakpoint at **reactor.core.publisher.Flux:8472** and using Evaluate Expression print stacktarce
```
e.printStackTrace();
```
And see:
```
java.lang.StackOverflowError
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.request(MonoFlatMapMany.java:112)
	at reactor.core.publisher.FluxConcatArray$ConcatArraySubscriber.request(FluxConcatArray.java:276)
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.request(MonoFlatMapMany.java:112)
	at reactor.core.publisher.FluxConcatArray$ConcatArraySubscriber.request(FluxConcatArray.java:276)
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.request(MonoFlatMapMany.java:112)
	at reactor.core.publisher.FluxConcatArray$ConcatArraySubscriber.request(FluxConcatArray.java:276)
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.request(MonoFlatMapMany.java:112)
	at reactor.core.publisher.FluxConcatArray$ConcatArraySubscriber.request(FluxConcatArray.java:276)
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.request(MonoFlatMapMany.java:112)
	at reactor.core.publisher.FluxConcatArray$ConcatArraySubscriber.request(FluxConcatArray.java:276)
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.request(MonoFlatMapMany.java:112)
	at reactor.core.publisher.FluxConcatArray$ConcatArraySubscriber.request(FluxConcatArray.java:276)
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.request(MonoFlatMapMany.java:112)
	at reactor.core.publisher.FluxConcatArray$ConcatArraySubscriber.request(FluxConcatArray.java:276)
	at reactor.core.publisher.MonoFlatMapMany$FlatMapManyMain.request(MonoFlatMapMany.java:112)
	...
```

## Additional debugging steps
It looks like source of problem is under **DefaultBridgedReactiveSession.class**

You can create breakpoint at **ReactiveRowMapperResultSetExtractor:64** then run job from step **2**.
Allowing program resume during paged request you will see increasing stacktrace.

## Issue & resolution
Issue:
https://github.com/spring-projects/spring-data-cassandra/issues/1215

Resolution:
https://github.com/spring-projects/spring-data-cassandra/commit/7395a7e269d9ebb2f17edeb4a095046a466a8ab8

Problem will disappear in following versions:
- [3.2.9 (2021.0.9)](https://github.com/spring-projects/spring-data-cassandra/milestone/213)
- [3.3.2 (2021.1.2)](https://github.com/spring-projects/spring-data-cassandra/milestone/214)

Big thanks to [Mark Paluch](https://github.com/mp911de) for resolving that!
