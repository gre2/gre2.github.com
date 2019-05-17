---
layout: post
title: "rocketmq-store"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

# Rocketmq-store

![image](https://ws3.sinaimg.cn/large/87a42753ly1g33984buz9j21gk0t2gsv.jpg)

### 流程

DefaultMessageStore.putMessage

CommitLog.putMessage

**CommitLog.lockForPutMessage操作commitlog的时候上锁，消息逻辑串行化，自旋锁的方式获取commitlog的锁**

MappdFile.appendMessage  同时注册DefaultAppendMessageCallback回调

CommitLog.内部类DefaultAppendMessageCallback.doAppend写入byteBuffer

**CommitLog.releasePutMessageLock解锁**

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



![image](https://ws2.sinaimg.cn/large/87a42753ly1g32zy31yqkj212c0kw129.jpg)

FlushRealTimeService(异步写)：每500ms对CommitLog进行一次Flush，当新写入数据超过16KB，或者距离上次Flush的时间间隔超过10S，将CommitLog位于内存中的数据同步到磁盘文件。

### HA主从同步

HAService

