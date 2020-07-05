---
layout: post
title: "countDownLatch应用"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

## countDownLatch

#### 应用

```
public class Test{
    
    @Resource
    private ThreadPoolTaskExecutor mafkaThreadPool;//线程池
    
    public void flow(){
    	final CountDownLatch latch = new CountDownLatch(5);
    	for (int i=0;i<5;i++) {
        	mafkaThreadPool.execute(() -> {
                try {
                     //逻辑
                    } catch (Exception e) {
                           
                    } finally {
                         latch.countDown();
                    }
            });
        }

	latch.await();
	//逻辑
}
```

#### 原理

* 初始化的时候给state赋值，代表countDownLatch的剩余次数
* await的时候判断计数器是否等于0，不等于创建Node节点，加入AQS阻塞队列挂起
* down的时候通过释放锁对计数器减一，当state=0的时候唤醒AQS阻塞队列里面的全部节点