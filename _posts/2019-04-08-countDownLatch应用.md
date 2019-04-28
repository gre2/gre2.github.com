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

