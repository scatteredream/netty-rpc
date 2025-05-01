# 基于 Netty 的 RPC 框架

[碎梦：基于 Netty 的 RPC 框架（代码解析）](https://scatteredream.github.io/2025/02/03/rpc-interpretation) 

[问题补充](https://docs.qq.com/doc/DY3Joc3RvbHVGVGta)

## 介绍

一款基于 Netty + SpringBoot 实现的自定义 RPC 框架。

同时引入其他通信方式如 Http、Socket ，注册中心引入了 Zookeeper、Nacos

----------------

### 项目实现内容

- [x] 实现基于 Netty/Socket/Http 三种方式进行网路通信
- [x] 自定义消息协议，编解码器
- [x] 五种序列化算法（JDK、JSON、HESSIAN、KRYO、PROTOSTUFF）
- [x] 三种负载均衡算法（RoundRobin、Random、ConsistentHash）
- [x] 两种动态代理（JDK、CGLIB）
- [x] 基于 Zookeeper 的服务注册与发现，增加服务本地缓存与监听，基于 Nacos 的服务注册与发现
- [x] 集成 Spring，自定义注解提供 RPC 组件扫描、服务注册、服务消费
- [x] 集成 SpringBoot，完成自动配置
- [x] 增加 Netty 心跳机制
- [x] 实现自定义 SPI 机制，用户可以自行实现指定接口

----

### RPC概述

RPC 又称远程过程调用（Remote Procedure Call），用于解决分布式系统中服务之间的调用问题。通俗地讲，就是开发者能够像调用本地方法一样调用远程的服务。一个最基本的RPC框架的基本架构如下图所示：

<img src="https://pub-9e727eae11e040a4aa2b1feedc2608d2.r2.dev/PicGo/简单RPC架构图.png" alt="简单RPC架构图" style="zoom: 45%;" />

RPC框架一般必须包含三个组件，分别是**客户端、服务端**以及**注册中心**，一次完整的 RPC 调用流程一般为：

1. 服务端启动服务后，将他提供的服务列表发布到注册中心（服务注册）；
2. 客户端会向注册中心订阅相关的服务地址（服务订阅）；
3. 客户端通常会利用本地代理模块 Proxy 向服务端发起远程过程调用，Proxy 负责将调用的方法、参数等数据转化为网络字节流；
4. 客户端从服务列表中根据负载均衡策略选择一个服务地址，并将数据通过网络发送给服务端；
5. 服务端得到数据后，调用对应的服务，然后将结果通过网络返回给客户端。

虽然 RPC 调用流程很容易理解，但是实现一个完整的 RPC 框架设计到很多内容，例如服务注册与发现、通信协议与序列化、负载均衡、动态代理等，下面我们一一进行初步地讲解。

## 项目结构介绍

<img src="https://pub-9e727eae11e040a4aa2b1feedc2608d2.r2.dev/PicGo/项目架构图.png" alt="项目架构图" style="zoom:67%;" />

`consumer`模块：服务的消费者，依赖于 `rpc-client-spring-boot-starter` 模块；

`provider-api`模块：服务提供者暴露的API；

`provider`模块：服务的提供者，依赖于 `rpc-server-spring-boot-starter` 模块：

`rpc-client-spring-boot`模块：rpc 客户端模块，封装客户端发起的请求过程，提供服务发现、动态代理，网络通信等功能；

`rpc-client-spring-boot-stater`模块：是`rpc-client-spring-boot`的stater模块，负责引入相应依赖进行自动配置；

`rpc-framework-core`模块：是rpc核心依赖，提供负载均衡、服务注册发现、消息协议、消息编码解码、序列化算法；

`rpc-server-spring-boot`模块：rpc 服务端模块，负责启动服务，接受和处理RPC请求，提供服务发布、反射调用等功能；

`rpc-server-spring-boot-stater`模块：是`rpc-server-spring-boot`的stater模块，负责引入相应依赖进行自动配置；

## 运行项目

1、首先需要安装并启动 zookeeper；

2、修改 Consumer 和 Provider 模块下的 application.yml 的注册中心地址属性，即 rpc.client.registry-addr=你的zk连接地址，服务端则配置 rpc.server.registry-addr属性；

3、先启动 Provider 模块，正常启动 SpringBoot 项目即可，本项目使用基于 SpringBoot 的自动配置，运行后会自动向 SpringIOC 容器中创建需要的 Bean 对象。

4、然后启动 Consumer 模块，通过 Controller 去访问服务进行 rpc 调用了。



## 环境搭建

- 操作系统：Windows + Linux
- 集成开发工具：IntelliJ IDEA
- 项目技术栈：SpringBoot 2.5.2 + JDK 1.8 + Netty 4.1.65.Final
- 项目依赖管理工具：Maven 4.0.0
- 注册中心：Zookeeeper 3.7.1

## 项目测试

- 启动 Zookeeper 服务器：进入到zk的bin目录，输入命令 `./zkServer.sh`
- 启动 provider 模块 ProviderApplication
- 启动 consumer 模块 ConsumerApplication
- 测试：浏览器输入 http://localhost:8080/hello/zhangsan ，成功返回：`hello, zhangsan`，rpc 调用成功。
- 调用接口 100 次耗时 26ms，调用 10_0000 次耗时 25164 ms。

## 压力测试

**[JMH](https://zhuanlan.zhihu.com/p/434083702)**

`JMH`即`Java Microbenchmark Harness`，是`Java`用来做基准测试的一个工具，该工具由`OpenJDK`提供并维护，测试结果可信度高。

相对于 Jmeter、ab ，它通过编写代码的方式进行压测，在特定场景下会更能评估某项性能。

本次通过使用 JMH 来压测 RPC 的性能（官方也是使用JMH压测）

启动 10000 个线程同时访问 sayHello 接口，总共进行 3 轮测试，测试结果如下：

```
Benchmark                                          Mode     Cnt      Score       Error  Units
BenchmarkTest.testSayHello                        thrpt       3  29288.573 ± 20780.318  ops/s
BenchmarkTest.testSayHello                         avgt       3      0.532 ±     6.159   s/op
BenchmarkTest.testSayHello                       sample  395972      0.382 ±     0.002   s/op
BenchmarkTest.testSayHello:testSayHello·p0.00    sample              0.003               s/op
BenchmarkTest.testSayHello:testSayHello·p0.50    sample              0.318               s/op
BenchmarkTest.testSayHello:testSayHello·p0.90    sample              0.387               s/op
BenchmarkTest.testSayHello:testSayHello·p0.95    sample              0.840               s/op
BenchmarkTest.testSayHello:testSayHello·p0.99    sample              2.282               s/op
BenchmarkTest.testSayHello:testSayHello·p0.999   sample              2.470               s/op
BenchmarkTest.testSayHello:testSayHello·p0.9999  sample              2.496               s/op
BenchmarkTest.testSayHello:testSayHello·p1.00    sample              2.508               s/op
BenchmarkTest.testSayHello                           ss       3      0.118 ±     0.051   s/op
```

测试曲线图：

<img src="https://pub-9e727eae11e040a4aa2b1feedc2608d2.r2.dev/PicGo/rpc10000并发测试结果.png">

同时，在同样的条件下，启动 5000（1w个电脑会卡死） 个线程同时对 **Dubbo2.7.14** 发起 RPC 调用，得到的结果如下：

```
Benchmark                                       Mode     Cnt      Score      Error  Units
StressTest.testSayHello                        thrpt       3  41549.866 ± 9703.455  ops/s
StressTest.testSayHello                         avgt       3      0.119 ±    0.034   s/op
StressTest.testSayHello                       sample  611821      0.123 ±    0.001   s/op
StressTest.testSayHello:testSayHello·p0.00    sample              0.042              s/op
StressTest.testSayHello:testSayHello·p0.50    sample              0.119              s/op
StressTest.testSayHello:testSayHello·p0.90    sample              0.129              s/op
StressTest.testSayHello:testSayHello·p0.95    sample              0.139              s/op
StressTest.testSayHello:testSayHello·p0.99    sample              0.195              s/op
StressTest.testSayHello:testSayHello·p0.999   sample              0.446              s/op
StressTest.testSayHello:testSayHello·p0.9999  sample              0.455              s/op
StressTest.testSayHello:testSayHello·p1.00    sample              0.456              s/op
StressTest.testSayHello                           ss       3      0.058 ±    0.135   s/op
```

<img src="https://pub-9e727eae11e040a4aa2b1feedc2608d2.r2.dev/PicGo/dubbo5000并发测试结果.png">

**结果**：

|            | RPC     | RPC   | Dubbo2.7.14 |
| ---------- | ------- | ----- | ----------- |
| 并发数     | 10000   | 5000  | 5000        |
| TPS        | 29288   | 31675 | 41549       |
| RTT        | 95% 8ms | xxx   | 95% 50ms    |
| AVGTime/OP | 0.532   | 0.532 | 0.119       |
| OOM        | 无      | 无    | 无          |

对比了 jmeter、Apache-Benmark（ab）、jmh 这三个压测工具，个人比较推荐使用jmh，原因有：

- jmh压测简单，只需要引入依赖，声明注解
- 准确性高，目前大多数性能压测都是使用jmh
- 缺点就是代码入侵