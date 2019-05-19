---
layout: post
title: "rocketmq-store"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

# Rocketmq-store

![](http://ww1.sinaimg.cn/large/87a42753ly1g34ko4nsilj21gk0t210q.jpg)

### 流程

DefaultMessageStore.putMessage

CommitLog.putMessage

**CommitLog.lockForPutMessage操作commitlog的时候上锁，消息逻辑串行化，自旋锁的方式获取commitlog的锁**

MappdFile.appendMessage  同时注册DefaultAppendMessageCallback回调

CommitLog.内部类DefaultAppendMessageCallback.doAppend写入byteBuffer

**CommitLog.releasePutMessageLock解锁**



### ReputMessageService重放消息，每1ms一次

- 该服务不断生成 消息位置信息 到 消费队列(ConsumeQueue)
- 该服务不断生成 消息索引 到 索引文件(IndexFile)

![](http://ww1.sinaimg.cn/large/87a42753ly1g35rp9u4i3j21ea0do3z9.jpg)

doReput方法

> 当 `Broker` 是主节点 && `Broker` 开启的是长轮询，通知消费队列有新的消息。
>
> `NotifyMessageArrivingListener` 会 调用 `PullRequestHoldService#notifyMessageArriving(...)` 方法

### DefaultMessageStore#doDispatch(…)

* 非事务消息 或 事务提交消息 建立 消息位置信息 到 ConsumeQueue[`putMessagePositionInfo`]

  > 根据topic，queueid找到consumequeue
  >
  > 添加位置信息封装putMessagePositionInfoWrapper
  >
  > 设置commit log重放消息到consume queue，插入mappedFile

* 建立 索引信息 到 IndexFile[`buildIndex`]

## FlushConsumeQueueService.doFlush





### 刷盘

```java
class MappedFile{
    /**
     * 当前写入位置，下次开始写入的开始位置
     */
    protected final AtomicInteger wrotePosition = new AtomicInteger(0);
    /**
     * 当前文件的提交指针，如果开启 transientStore­PoolEnable，
     * 则数据会存储在 TransientStorePool 中， 然后提交到内存映射 ByteBuffer 中， 再 刷写到磁盘。
     */
    protected final AtomicInteger committedPosition = new AtomicInteger(0);
    /**
     * 当前flush位置，刷写到磁盘指针，该指针之前的数据持久化到磁盘中 。
     */
    private final AtomicInteger flushedPosition = new AtomicInteger(0);
}
```



![](http://ww1.sinaimg.cn/large/87a42753ly1g34knnno7fj212c0kwjzp.jpg)

CommitRealTimeService(开启写入缓冲池)：将缓冲池中的数据Commit到CommitLog的FileChannel中

FlushRealTimeService(异步写)：每500ms对CommitLog进行一次Flush，当新写入数据超过16KB，或者距离上次Flush的时间间隔超过10S，将CommitLog位于内存中的数据同步到磁盘文件。

### HA主从同步

HAService

### consumequeue的存储

主要有两个组件：

- `ReputMessageService` ：write ConsumeQueue。
- `FlushConsumeQueueService` ：flush ConsumeQueue。

### CleanCommitLogService

每隔10S执行一次清理失效CommitLog日志文件，默认清理72h之前的



