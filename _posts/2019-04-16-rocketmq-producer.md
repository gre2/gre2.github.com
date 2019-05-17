---
layout: post
title: "rocketmq-producer"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

# Rocketmq-producer

### 总结

 ```java
  org.apache.rocketmq.example.ordermessage.Producer
 ```

* producer发送消息时，默认按顺序依次选择messagequeue发送，当某个broker宕机时，其messagequeue发送不成功，那么会按顺讯找到下一个不是此broker的messagequeue发送

* **全局顺序**：顺序发送是前提，选择同步发送形式，借助MessageQueueSelector将此topic的所有消息发送到同一个messageQueue，且只能是一个producer

* **局部顺序**:和全局的不同就是不用发到一个producer中

### 类属性

* MQClientManager#factoryTable[ConcurrentHashMap<String/* clientId */, MQClientInstance>]
* MQClientInstance#producerTable[ConcurrentHashMap<String/* group */, MQProducerInner>]
* DefaultMQProducerImpl#topicPublishInfoTable[ConcurrentHashMap<String/* topic */, TopicPublishInfo>]
* NettyRemotingAbstract#responseTable[ConcurrentHashMap<Integer /* opaque */, ResponseFuture>]
* NettyRemotingAbstract#responseTable[ConcurrentHashMap<Integer /* opaque */, ResponseFuture>]

### 流程

* 在DefaultMQProducerTest创建MQClientInstance[mQClientFactory]

* 创建生产者组producerGroup

* 用生产者组创建DefaultMQProducer[producer]

* 创建message[String topic, byte[] body]

* producer.start()

  > 设置初始化字段
  >
  > 检查生产者组信息
  >
  > ClientConfig[instanceName]为UtilAll.getPid()
  >
  > ![image](https://wx1.sinaimg.cn/mw690/87a42753ly1g2jjslz2udj21b00rudlx.jpg)
  >
  > 从MQClientManager#factoryTable中根据clientId获得MQClientInstance[mQClientFactory]对象
  >
  > 从MQClientInstance#producerTable中根据生产者组获得生产者是否创建
  >
  > DefaultMQProducerImpl#topicPublishInfoTable创建一组topic对象
  >
  > MQClientInstance#mQClientFactory.start()
  >
  > > ![image](https://wx2.sinaimg.cn/mw690/87a42753ly1g2jkfw4xf1j21a80yun81.jpg)
  > >
  > > 判断clientConfig是否有nameservAddr，没有的话远程获取
  > >
  > > ##### MQClientAPIImpl(客户端与远程交互的封装,其内部使用了RemotingClient来实现与远程的交互)
  > >
  > > MQClientAPIImpl#mQClientAPIImpl.start()——> NettyRemotingClient.start()
  > >
  > > 创建DefaultEventExecutorGroup#defaultEventExecutorGroup
  > >
  > > 设置scanResponseTable定时任务
  > >
  > > 启动一系列的定时任务MQClientInstance#startScheduledTask[获取nameserv，拉取topic路由，定时同步消费进度，持久化消费进度，调整线程池]见下方详解
  > >
  > > this.pullMessageService.start();
  > >
  > > this.pullMessageService.start();
  > >
  > > 设置状态是running
  >
  > 发送心跳到Broker，上传过滤类源码到Filtersrv#sendHeartbeatToAllBrokerWithLock

* DefaultMQProducerImpl#sendDefaultImpl

  > 确认producer的状态是否是running
  >
  > 检查topic和消息体
  >
  > tryToFindTopicPublishInfo获取Topic的路由信息
  >
  > >  根据topic从topicPublishInfoTable取出此topic的对象，如果没有可用topic，则根据topic调用updateTopicRouteInfoFromNameServer方法
  > >
  > >  > RemotingCommand#RequestCode.GET_ROUTEINTO_BY_TOPIC
  > >  >
  > >  > NettyRemotingClient#invokeSync()
  > >  >
  > >  > > getAndCreateChannel()#channelTables里维护连接
  > >  > >
  > >  > > > 获取channel###createChannel()#this.bootstrap.connect()#Bootstrap.connect()#this.doConnect()#Bootstrap.doConnect0()#Bootstrap.channel.eventLoop().execute()#NettyRemotingClient.shutdown()
  > >  > >
  > >  > > rpcHook.doBeforeRequest()
  > >  > >
  > >  > > this.invokeSyncImpl()
  > >  > >
  > >  > > >维护responseTable，记录本次请求和请求响应的关系
  > >  > > >
  > >  > > >channel.writeAndFlush(request).addListener()
  > >  > > >
  > >  > > >DefaultRequestProcessor#getRouteInfoByTopic()
  > >  > >
  > >  > > rpcHook.doAfterResponse()
  > >  > >
  > >  > > This.closeChannel()
  > >
  > >  新增topicPublishInfoTable
  >
  > 



















### 





### MQClientInstance#startScheduledTask

>```java
>this.startScheduledTask();
>```
>
>启动各种定时任务
>
>* 每两分钟执行一次寻址服务(NameServer地址)
>* 每30秒更新一次所有的topic的路由信息(topicRouteTable)
>* 每30秒移除离线的broker ， 每30秒发送一次心跳给所有的master broker
>* 每5秒提交一次消费的offset（逻辑偏移量）到broker（broker端为ConsumerOffsetManager负责记录）
>* 每1分钟调整一次线程池，这也是针对消费者来说的，具体为如果消息堆积超过10w条，则调大线程池，最多64个线程，如果消息堆积少于8w条，则调小线程池，最少20个线程

### MQClientInstance#pullMessageService.start();

> consumer的拉取消息线程方式实现：pullMessageService继承ServiceThread（对拉取消息请求进行了封装，使其队列化），start拉取消息线程启动，在run方法里面实现了pullMessageService#run），不断的从pullRequestQueue中取出请求，并调用消息拉取（pullMessageService#pullMessage）

