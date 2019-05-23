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

### 特点

高可用，由于各个nameserver之间并无通信，故一个namerserver挂了并不会影响其他namerserver

任何producer，consumer，broker与所有nameserver通信，都是单向的，这种机制保证rocketmq水平扩容变的很容易

nameserv只存储broker的信息，剩下的信息全部存储在broker上面

### 实现

* 初始化namservController，包含namesrvConfig（namesrv相关配置），nettyServerConfig（netty的相关配置），KVConfigManager（KV配置管理），RouteInfoManager（路由信息、topic信息管理），BrokerHousekeepingService（broker管理服务）
* NamesrvController.initialize()：加载KV配置，初始化通讯层（Netty的初始化：remotingServer对象），初始化线程池remotingExecutor，向remotingServer对象中注册DefaultRequestProcessor对象
* 启动定时扫描notActive的broker任务；

### 类属性

##### KVConfigManager#configTable

> ```java
> HashMap<String/* Namespace */, HashMap<String/* Key */, String/* Value */>> 
> ```

##### RouteInfoManager#topicQueueTable

> ```java
> HashMap<String/* topic */, List<QueueData>>
> ```
>
> ```java
> public class QueueData implements Comparable<QueueData> {
>     /** Broker名*/
>     private String brokerName;
>     /**读队列长度*/
>     private int readQueueNums;
>     /**写队列长度*/
>     private int writeQueueNums;
>     /** 读写权限*/
>     private int perm;
>     private int topicSynFlag;
> }
> ```

##### RouteInfoManager#brokerAddrTable

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

##### RouteInfoManager#clusterAddrTable

> ```java
> HashMap<String/* clusterName */, Set<String/* brokerName */>>
> ```

##### RouteInfoManager#brokerLiveTable  [broker地址与broker连接信息]

> ```java
> HashMap<String/* brokerAddr */, BrokerLiveInfo>
> ```
>
> ```java
> class BrokerLiveInfo {
>     /**最后更新时间*/
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

  