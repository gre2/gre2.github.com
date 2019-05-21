---
layout: post
title: "rocketmq-broker"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

# Rocketmq-broker

### 特点

broker挂了并不影响其他broker

namerserver挂了对broker的影响？？？

> 由于broker默认每30秒向所有的nameserver进行注册
> 如果nameserver挂了，那么broker会将连接移除
> 下次注册时会继续尝试连接该nameserver，但是会连接失败，所以，broker端会定时的输出连接失败的日志

### 功能

* 【生产者】接受客户端发送到消息，并存储
* 【消费者】接受客户端的消费请求，返回消息数据
* consumergroup消费进度存储、查询
* topic配置管理
* 一些其他的配置管理、查询：topic创建、删除、修改，broker配置查询、修改，消息查询，broker运行状态，producer、consumer连接，消费进度等

### 组件

##### 类属性

BrokerController#BrokerConfig

> broker的一些基础配置 

BrokerController#messageStoreConfig

> 包含一些数据存储相关的配置

BrokerController#consumerOffsetManager

> consumer消费进度管理者

BrokerController#messageStore

> 消息存储实现，参照DefaultMessageStore

BrokerController#topicConfigManager

> topic配置管理

BrokerController#pullMessageProcessor

> 客户端拉取消息请求处理

BrokerController#remotingServer

> 远程服务

##### 方法

BrokerController#registerProcessor()

>  注册一系列处理器来处理客户端请求，或响应客户端请求
>
> SendMessageProcessor处理客户端发送的消息处理器
>
> PullMessageProcessor处理客户端拉取消息处理器
>
> QueryMessageProcessor根据消息的key或者消息id查询消息
>
> ClientManageProcessor客户端连接管理(包括客户端心跳，客户端连接注销，根据consumergroup查询consumer id列表,客户端offset上报,客户端offset查询) 
>
> AdminBrokerProcessor broker管理类请求处理



### broker-intialize

- 加载topic配置（topicConfigManager）

- 加载消费进度存储（consumerOffsetManager）

- 加载订阅组配置管理（subscriptionGroupManager）

- 初始化存储层（DefaultMessageStore）

  > ```java
  > //用于broker层的消息落地存储，超级复杂
  > this.messageStore =
  >     new DefaultMessageStore(this.messageStoreConfig, this.brokerStatsManager, this.messageArrivingListener,
  >         this.brokerConfig);
  > ```

- 初始化通讯层（remotingServer和fastRemotingServer）

- 初始化线程池（sendMessageExecutor处理发送消息线程池，pullMessageExecutor处理拉取消息线程池，adminBrokerExecutor处理管理Broker线程池，clientManageExecutor处理管理Client线程池）

- 将线程池（SendMessageProcessor,pullMessageProcessor,QueryMessageProcessor,ClientManageProcessor,EndTransactionProcessor,AdminBrokerProcessor）注册到netty消息处理器当中（remotingServer和fastRemotingServer对象中）

- 启动相关执行任务（每天凌晨统计消息量的任务（brokerStats.record()），定时持久化消费进度(consumerOffsetManager.persist())）

- 获取NameServer地址或者开启定时获取NameServer地址任务

- 如果是slave：开启Slave定时从Master同步配置信息任务，如果是Master，开启增加统计日志任务；

### broker-start

- 启动刚刚初始化的各个管理器（消息存储启动（DefaultMessageStore.start()）；通信层启动(remotingServer.start(),fastRemotingServer.start())；brokerOuterAPI.start()；pullRequestHoldService.start()；

- 启动检测所有客户端连接（clientHousekeepingService.start()）；filterServerManager.start()；brokerStatsManager.start()；brokerFastFailure.start()）

- 启动时，强制注册:registerBrokerAll()

- 开启定时把Broker注册到NameServer的任务

### 类作用

ReputMessageService：Broker在启动时，启动ReputMessageService，其每隔10ms为CommitLog里的消息生成位置信息PositionInfo，存入此消息对应的ConsumeQueue。一个ConsumeQueue对应着一个MessageQueue，Consume拉取消息时首先从ConsumeQueue里获取PositionInfo，对比tag是否匹配。匹配的话提取其在CommitLog的Offset，msg size，然后去CommitLog里查询消息。

Consumer的每次消息拉取会指定一个进度，这个进度就是指ConsumeQueue里的进度，也就是从哪个位置开始提取PositionInfo，同时Broker会定期的持久化每个ConsumeQueue的消费进度。当消费请求提交的进度已经达到ConsumeQueue的最大值，也就是没有新的消息生成时，Broker挂起请求。每隔5S查看CnsumeQueue的maxOffset是否超过请求提交的Offset，如果是，则执行消息拉取。当挂起的时间超过15S时，强制结束请求。每当ConsumeQueue有新的PositionInfo生成时，匹配tag，若符合，立即唤醒消息拉取请求。执行拉取流程。

ReputMessageService生成PositionInfo的同时，也会生成IndexInfo，也就是消息的索引信息，存放在Index目录下的IndexFile中。Producer在发送消息时，会为每条消息生成一个唯一的key，当然也可以自定义key。唯一的key会在发送结果中返回，用户可以使用这些key查询发送的消息。也就是说获取消息可以通过ConsumeQueue，也可以通过Index。

