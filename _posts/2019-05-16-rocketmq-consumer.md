layout: post
title: "rocketmq-consumer"
description: ""
category: [java,基础]
tags: [基础]

{% include JB/setup %}

# rocketmq-consumer

### 前期工作

检查配置

复制订阅信息

>在DefaultMQPushConsumer中获取订阅信息Map<String /* topic */, String /* sub expression */> subscription
>
>循环构建subscription不同topic下的subscriptionData，之后构建RebalanceImpl的ConcurrentHashMap<String /* topic */, SubscriptionData> subscriptionInner
>
>重复上面的两步，构建retryTopic[%RETRY%FooBarGroup1558007931736]的DefaultMQPushConsumer.subscriptionData，RebalanceImpl.subscriptionInner

获取MQClientFactory对象

构建RebalanceImpl对象

>消息分配策略，集群消费时用到该类，该类为消费者分配queue，默认实现是AllocateMessageQueueStrategy

```java
public abstract class RebalanceImpl {
   
    /**
     * 消息队列 和 消息处理队列 Map
     */
    protected final ConcurrentHashMap<MessageQueue, ProcessQueue> processQueueTable = new ConcurrentHashMap<>(64);
    /**
     * Topic 和 消息队列 订阅Map
     */
    protected final ConcurrentHashMap<String/* topic */, Set<MessageQueue>> topicSubscribeInfoTable = new ConcurrentHashMap<>();
    /**
     * Topic 和 订阅数据 Map
     */
    protected final ConcurrentHashMap<String /* topic */, SubscriptionData> subscriptionInner = new ConcurrentHashMap<>();
    /**
     * 消费分组
     */
    protected String consumerGroup;
    /**
     * 消息模型
     */
    protected MessageModel messageModel;
    /**
     * 消息分配策略
     */
    protected AllocateMessageQueueStrategy allocateMessageQueueStrategy;
    /**
     * MQ客户端对象
     */
    protected MQClientInstance mQClientFactory;
}    
```

构建消费拉取消息PullAPIWrapper

初始化offsetStore[RemoteBrokerOffsetStore]

![image](https://ws2.sinaimg.cn/large/87a42753ly1g33f19h2dmj20nu05emyc.jpg)



```java
this.pullMessageService.start();
```

```java
private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<>();
```
执行**PullMessageService.run**方法
```java
@Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStopped()) {
            try {
                PullRequest pullRequest = this.pullRequestQueue.take();
                if (pullRequest != null) {
                    this.pullMessage(pullRequest);
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                log.error("Pull Message Service Run Method exception", e);
            }
        }

        log.info(this.getServiceName() + " service end");
    }
```

DefaultMQPushConsumerImpl.PullCallback回调，处理消费者的拉取结果

PullAPIWrapper.pullKernelImpl拉取消息，**发送RequestCode.PULL_MESSAGE的请求**



RebalanceService在20s之后启动

MQClientInstance.doRebalance，循环消费者分组ConcurrentHashMap<String/* group */, MQConsumerInner> consumerTable，**具体逻辑实现在RebalanceService.doRebalance**

```java
public void doRebalance(final boolean isOrder) {
        // 分配每个 topic 的消息队列
        Map<String, SubscriptionData> subTable = this.getSubscriptionInner();
        if (subTable != null) {
            for (final Map.Entry<String, SubscriptionData> entry : subTable.entrySet()) {
                final String topic = entry.getKey();
                try {
                    this.rebalanceByTopic(topic, isOrder);
                } catch (Throwable e) {
                    if (!topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                        log.warn("rebalanceByTopic Exception", e);
                    }
                }
            }
        }
        // 移除未订阅的topic对应的消息队列
        this.truncateMessageQueueNotMyTopic();
}
```

```java
// 重新均衡
this.mQClientFactory.rebalanceImmediately();
```



### 拉取消息

```java
pullMessageService.executePullRequestImmediately(createPullRequest());
```

![image](https://ws2.sinaimg.cn/large/87a42753ly1g3453g7rnpj224s0w411w.jpg)

```java
private PullRequest createPullRequest() {
    PullRequest pullRequest = new PullRequest();
    pullRequest.setConsumerGroup(consumerGroup);
    pullRequest.setNextOffset(1024);

    MessageQueue messageQueue = new MessageQueue();
    messageQueue.setBrokerName(brokerName);
    messageQueue.setQueueId(0);
    messageQueue.setTopic(topic);
    pullRequest.setMessageQueue(messageQueue);
    ProcessQueue processQueue = new ProcessQueue();
    processQueue.setLocked(true);
    processQueue.setLastLockTimestamp(System.currentTimeMillis());
    pullRequest.setProcessQueue(processQueue);

    return pullRequest;
}
```

```java
public void executePullRequestImmediately(final PullRequest pullRequest) {
    try {
        this.pullRequestQueue.put(pullRequest);
    } catch (InterruptedException e) {
        log.error("executePullRequestImmediately pullRequestQueue.put", e);
    }
}
```

```java
private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<>();
```

countDownLatch阻塞了，执行PullMessageService的run方法，也就是pullMessage(final PullRequest pullRequest) 

根据requestCode执行PullMessageProcessor.processRequest

执行DefaultMessageStore.getMessage()，从commitlog读取数据，更新偏移量，计算剩余的偏移量。

还有PullCallback回调一些逻辑：

>  设置下次拉取消息的队列位置，提交拉取到的消息到消息处理队列

```java
boolean dispathToConsume = processQueue.putMessage(pullResult.getMsgFoundList());
```



### 延迟消息

ScheduleMessageService#DeliverDelayedMessageTimerTask#executeOnTimeup

消费者发回消息时，可以指定延迟级别，默认级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h，也就是说delayLevel = 3代表延迟10秒后重投递，最大重试次数16对应着2h后投递，每多消费一次投递时间就增长到下个阶段。当延迟级别delayLevel < 0 或超过最大消费次数时，放入Dead Letter Queue，topic名称格式为：%DLQ%+consumeGroup，默认queueId=0，死信队列只能写入，不能消费，这在创建topic时就指定的。

### 顺序投递延迟消息

Consumer消费消息时，如果返回RECONSUME_LATER，或者主动的sendMessageBack(…，int delayLevel)时，会将消息发回给Broker，Broker对消息做个封装，指定topic为SCHEDULE_TOPIC_XXXX，QueudId=delayLevel-1，若未指定delayLevel，默认是ReConsumeTimes + 3，将封装后的消息存入CommitLog，ReputMessageService为其生成PositionInfo，tagsCode存储延时投递时间，存入”SCHEDULE_TOPIC_XXXX”的ConsumeQueue中。delayLevel有16个，因此最多情况下SCHEDULE_TOPIC_XXXX会有16个ConsumeQueue。Broker启动时，ScheduleMessageService会启动16个线程对应16个delayLevel的读取服务，有序的读取ConsumeQueue里的PositionInfo。ScheduleMessageService会在 [当前时间<=延时投递时间] 时从CommitLog中提取这消息，去除封装，抹去delayLevel属性，从新存入CommitLog，并马上更新延时投递偏移量dealyOffset。ReputMessageService再次为当前消息生成PositionInfo，因为不存在delayLevel，PositionInfo存入Topic为%RETRY%+consumeGroup，queueId为0的ConsumeQueue中。每个消费者在启动时都订阅了自身消费者组的重试队列，当重试队列里有位置信息时，拉取相应消息进行重新消费。消息的第一次重试会发回给原始的消费者(执行sendMessageBack的消费者)，之后的多次重试统一由订阅了QueueId = 0 的消费者消费。 

![image](https://ws2.sinaimg.cn/large/87a42753ly1g339tqyzpmj21du0hctde.jpg)