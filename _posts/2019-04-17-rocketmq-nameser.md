---
layout: post
title: "rocketmq-namserv"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

# Rocketmq-namserv

![](http://ww1.sinaimg.cn/large/87a42753ly1g34kv3o90ij21ga0sy4ai.jpg)

### 疑问

1.路由中心存储的是什么数据？

2.如何避免nameserver的单点故障，提高可用性？

3.消息生产者如何知道消息要发往哪台消息服务器？（nameserver应运而生）

4.如果某一个消息服务器宕机了，那么生产者如何在不重启服务的情况下感知？（nameserver应运而生）

5.nameserver和broker之间如何保持长连接？（代码层级的实现）

6.为什么broker宕机，路由注册表将其移除，但是不会马上通知生产者，为什么这样设计？

### 解疑

1.为消息生产者和消费者提供关于主题topic的路由元信息，那么nameserver能够存储路由的元信息，所以还需要管理broker节点[包括路由注册，路由删除等功能]

2.通过部署多台nameserver服务器来实现，但是彼此之间互不通信，也就是nameserver在某一时刻的数据并不完全相同，但这对消息发送不会造成任何影响

3

4.

5.

6.为了降低nameserver实现的复杂性，在消息发送端提供容错机制来保证消息发送的高可用性，3.4节再补充

### 特点

高可用，由于各个nameserver之间并无通信，故一个namerserver挂了并不会影响其他namerserver

任何producer，consumer，broker与所有nameserver通信，都是单向的，这种机制保证rocketmq水平扩容变的很容易

nameserv只存储broker的信息，剩下的信息全部存储在broker上面

### nameserver启动流程

1.业务参数nameServerConfig

2.网络参数NettyServerConfig

3.启动时 [javaD] [System.getProperty获取]

* 配置文件 -c configFile
* 启动命令 --属性名 属性值，例如--listenPort 9876

4.根据启动属性创建namesrvController实例

5.初始化namesrvController实例

* 加载配置文件
* 创建nettyRemotingServer网络处理对象
* 设置处理类 [DefaultRequestProcessor]
*  开启两个定时任务
  * 每10s扫描一次broker，移除处于不激活状态的broker
  * 每10分钟打印一次kv配置

6.注册JVM钩子，并启动NettyRemotingServer服务器，以便监听broker，消息生产者的网络请求

### 实现

* 初始化namservController，包含namesrvConfig（namesrv相关配置），nettyServerConfig（netty的相关配置），KVConfigManager（KV配置管理），RouteInfoManager（路由信息、topic信息管理），BrokerHousekeepingService（broker管理服务）
* NamesrvController.initialize()：加载KV配置，初始化通讯层（Netty的初始化：remotingServer对象），初始化线程池remotingExecutor，向remotingServer对象中注册DefaultRequestProcessor对象
* 启动定时扫描notActive的broker任务；

### 类属性

##### NamesrvConfig

> ```java
> //rocketmq主目录，System.getProperty第二个值是兜底值
> private String rocketmqHome = System.getProperty(MixAll.ROCKETMQ_HOME_PROPERTY, System.getenv(MixAll.ROCKETMQ_HOME_ENV));
> //存储kv配置属性的持久化路径
> private String kvConfigPath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "kvConfig.json";
> //默认配置文件路径，不生效，需要-c命令指定才生效
> private String configStorePath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "namesrv.properties";
> private String productEnvName = "center";
> private boolean clusterTest = false;
> private boolean orderMessageEnable = false;//是否支持顺序消息，默认不支持
> ```

##### NettyServerConfig

> ```java
> //nameserver的监听端口，该值默认会被初始化成9876
> private int listenPort = 8888;
> private int serverWorkerThreads = 8;//Netty业务线程池线程个数
> //Netty public任务线程池线程个数，Netty网络设计，根据业务类型会创建不同的线程池，比如处理消息发送、消息消费、心跳检测等。
> // 如果该业务类型（RequestCode）未注册线程池，则由public线程池执行。
> private int serverCallbackExecutorThreads = 0;
> //IO线程池线程个数，主要是NameServer、Broker端解析请求、返回相应的线程个数，这类线程主要是处理网络请求的，解析请求包，
> // 然后转发到各个业务线程池完成具体的业务操作，然后将结果再返回调用方
> private int serverSelectorThreads = 3;
> private int serverOnewaySemaphoreValue = 256;
> private int serverAsyncSemaphoreValue = 64;
> //网络连接最大空闲时间，默认120s。如果连接空闲时间超过该参数设置的值，连接将被关闭。
> private int serverChannelMaxIdleTimeSeconds = 120;
> 
> //网络socket发送缓存区大小，默认64k。
> private int serverSocketSndBufSize = NettySystemConfig.socketSndbufSize;
> //网络socket接收缓存区大小，默认64k。
> private int serverSocketRcvBufSize = NettySystemConfig.socketRcvbufSize;
> //ByteBuffer是否开启缓存，建议开启。
> private boolean serverPooledByteBufAllocatorEnable = true;
> //是否启用epoll io模型，linux环境建议开启
> private boolean useEpollNativeSelector = false;
> ```

##### KVConfigManager#configTable

> ```java
> HashMap<String/* Namespace */, HashMap<String/* Key */, String/* Value */>> 
> ```

##### RouteInfoManager#topicQueueTable [消息队列路由信息]

> ```java
> HashMap<String/* topic */, List<QueueData>>
> ```
>
> ```java
> public class QueueData implements Comparable<QueueData> {
>     /** Broker名*/
>     private String brokerName;
>     /**读队列长度，默认4个*/
>     private int readQueueNums;
>     /**写队列长度，默认4个*/
>     private int writeQueueNums;
>     /** 读写权限*/
>     private int perm;
>     private int topicSynFlag;
> }
> ```

##### RouteInfoManager#brokerAddrTable [broker基础信息]

> ```java
> HashMap<String/* brokerName */, BrokerData>
> ```
>
> ```java
> public class BrokerData implements Comparable<BrokerData> {
>     /**集群名*/
>     private String cluster;
>     /**Broker名*/
>     private String brokerName;
>     /**broker角色编号 和 broker地址 Map*/
>     private HashMap<Long/* brokerId */, String/* broker address */> brokerAddrs;
> }
> ```

##### RouteInfoManager#clusterAddrTable [broker集群信息]

> brokerName由相同的多台broker组成master-slave架构
>
> ```java
> HashMap<String/* clusterName */, Set<String/* brokerName */>>
> ```

##### RouteInfoManager#brokerLiveTable  [broker状态信息,nameserver每次收到心跳包都会替换该信息] 

> ```java
> HashMap<String/* brokerAddr */, BrokerLiveInfo>
> ```
>
> ```java
> class BrokerLiveInfo {
>     /**上次收到broker心跳包的时间*/
>     private long lastUpdateTimestamp;
>     /**数据版本号*/
>     private DataVersion dataVersion;
>     /**连接信息*/
>     private Channel channel;
>     /**ha服务器地址*/
>     private String haServerAddr;
> }
> ```

##### RouteInfoManager#filterServerTable  [broker地址 与 filtersrv数组 Map(用于类模式消息过滤)]

> ```java
> HashMap<String/* brokerAddr */, List<String>/* Filter Server */>
> ```

### nameserver路由注册，剔除，发现

![1570713302215.jpg](https://ws1.sinaimg.cn/large/87a42753ly1g7tf0i3e5oj213i0ky76r.jpg)

##### 注册

rocketmq路由注册是通过broker与nameserver的心跳功能实现的，broker启动时象集群中所有的nameserver发送心跳语句，之后每隔30s向集群中所有nameserver发送心跳包，nameserver收到broker心跳包时会更新brokerLiveTable缓存中的lastUpdateTimestamp，然后nameserver每10s扫描brokerLiveTable，如果连续120s没有收到心跳包，nameserver将移除该broker的路由信息同时关闭socket连接

1.broker发送心跳包 [BrokerController#start]

2.nameserver处理心跳包 [DefaultRequestProcessor]网络处理器解析请求类型

##### 剔除

1.brokerLiveTable中的lastUpdateTimesstamp时间戳距离当前时间超过120s，认为broker失效，移除该broker，关闭与broker连接

2.broker在正常被关闭的情况也会执行unregisterBroker指令

不管哪种方式剔除都会删除与该broker相关的信息topicQueueTable，brokerAddrTable，brokerLiveTable，filterServerTable

##### 发现 

非实时，当topic路由出现变化后，nameserver不主动推送给客户端，而是由客户端定时拉取主题最新的路由 [get_routeinfo_by_topic]

### namserv接收到broker的注册请求

org.apache.rocketmq.namesrv.processor.DefaultRequestProcessor#processRequest

> 如果topic配置信息发生变更或者是第一次为该broker注册，根据brokername及topicconfig（read、write queue数量等）新增或者更新到topicQueueTable中
>
> ```java
> private final HashMap<String/* topic */, List<QueueData>> topicQueueTable;
> ```

- 将当前请求注册的broker信息保存或者更新到clusterAddrTable，brokerAddrTable中
- 将当前请求注册的broker的topic信息保存或者更新到topicQueueTable中

broker定时上报，namserv定时更新

### nameserv和broker间的通讯

* broker在启动时，会加载当前broker上所有的topic信息，registerBroker

* nameserv与broker间维持着一个SocketChannel，长连接，broker第一次注册之后的10s后，每隔30s向其配置的所有namserv执行registerBroker，这就是broker和nameserv的心跳

```java
this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    BrokerController.this.registerBrokerAll(true, false, brokerConfig.isForceRegister());
                } catch (Throwable e) {
                    log.error("registerBrokerAll Exception", e);
                }
            }
        }, 1000 * 10, Math.max(10000, Math.min(brokerConfig.getRegisterNameServerPeriod(), 60000)), TimeUnit.MILLISECONDS);
```

* nameserv接受到broker传递的心跳信息时，如果是第一次心跳，创建brokerData，brokerLiveTable，保存其对象中的BrokerLiveInfo属性的dataVersion和lastUpdateTimestamp；如果不是第一次，那么更新其lastUpdateTimestamp和dataVersion。

```java
class BrokerData {
	private String cluster;	
	private String brokerName;
	private HashMap<Long/* brokerId */, String/* broker address */> brokerAddrs; 
}
```

```java
private final HashMap<String/* brokerAddr */, BrokerLiveInfo> brokerLiveTable;
```

```java
class BrokerLiveInfo {
	private long lastUpdateTimestamp;
	private DataVersion dataVersion;
	private Channel channel;
	private String haServerAddr;
}  
```

* 如果第一次心跳，而且broker是master，创建broker的queuedata,如果不是第一次心跳，dataVersion与nameserv上保持的不一致[RouteInfoManager#isBrokerTopicConfigChanged()]，更新。
* 如果当前broker是slave，那么将master的brokerAddr放入心跳注册结果中，返给slave，这样slave和master间就能进行数据传输。

```java
if (MixAll.MASTER_ID != brokerId) {
     String masterAddr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
     if (masterAddr != null) {
         BrokerLiveInfo brokerLiveInfo =this.brokerLiveTable.get(masterAddr);
         if (brokerLiveInfo != null) {
              result.setHaServerAddr(brokerLiveInfo.getHaServerAddr());
              result.setMasterAddr(masterAddr);
         }
      }
}
```

* nameserv维护着其他组件的SocketChannel对象，针对所有组件(broker，client)的长连接注册了ChannelEventListener，监听此SocketChannel的连接事件，当某个SocketChannel出现异常或者断开时（长连接断开不是心跳停止），会循环遍历所有broker的长连接，如果发现断开长连接属于某个broker，清除此broker的brokerdata和queuedata，不属于broker，则什么都不做。这样当broker变化不会通知client，这样client(producer，consumer)最晚需要30s才（下次请求）指定topic的TopicRouteData时，就不会包含此broker的数据了，也就是messagequeue上没有此broker的queue了。

NettyRemotingClient|NettyRemotingServer.start()注册client的长连接和nameserv

```java
this.nettyEventExecutor.start();
```

* nameserv每10s对所有broker的长连接进行扫描，发现lastUpdateTimestamp距离当前时间超过2分钟，断开长连接，清空数据，NameServContoller.scanNotActiveBroker定时任务

### nameserv和client的通信

* producer在发送消息的时候，根据消息topic查自身是否含有此topic相应的mesagequeue，没有就从nameserv处请求指定topic的TopicRouteData，也就是brokerData和queueData，然后构造messagequeue，之后启动定时任务定时更新

* consumer在启动之前就需要订阅topic，在启动时就会向namserv请求相应topic的TopicRouteData，同样的形式生成messagequeue，但和producer不同的是，consumer可以从slave拉取消息，不会过来master宕机的broker，consumer端也有定时任务

  ```java
  MQClientInstance.startScheduledTask()
  ```

  