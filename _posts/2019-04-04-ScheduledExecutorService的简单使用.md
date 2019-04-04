---
layout: post
title: "ScheduledExecutorService的使用"
description: ""
category: [java,实践]
tags: [实践]
---
{% include JB/setup %}

## ScheduledExecutorService的使用

##### 1.使用scheduleAtFixedRate()方法实现周期性执行

​    立刻执行，每隔100毫秒执行一次   

```
public class ScheduledExecutorServiceTest {
    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("run "+ System.currentTimeMillis());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
}
```

##### 2.使用Callable延迟运行

```
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CallableRun {
    public static void main(String[] args) {
        try {
            List<Callable> callableList = new ArrayList<>();
            callableList.add(new MyCallableA());
            callableList.add(new MyCallableB());
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            ScheduledFuture futureA = executorService.schedule(callableList.get(0), 4L, TimeUnit.SECONDS);
            ScheduledFuture futureB = executorService.schedule(callableList.get(1), 4L, TimeUnit.SECONDS);

            System.out.println("            X = " + System.currentTimeMillis());
            System.out.println("返回值A：" + futureA.get());
            System.out.println("返回值B：" + futureB.get());
            System.out.println("            Y = " + System.currentTimeMillis());

            executorService.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    static class MyCallableA implements Callable<String> {
        @Override
        public String call() throws Exception{
            try {
                System.out.println("callA begin " + Thread.currentThread().getName() + ", " + System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(3); // 休眠3秒
                System.out.println("callA end " + Thread.currentThread().getName() + ", " + System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "returnA";
        }
    }

    static class MyCallableB implements Callable<String>  {
        @Override
        public String call() throws Exception{
            System.out.println("callB begin " + Thread.currentThread().getName() + ", " + System.currentTimeMillis());
            System.out.println("callB end " + Thread.currentThread().getName() + ", " + System.currentTimeMillis());
            return "returnB";
        }
    }
}
```

结果 A begin，end，returnA，B begin，end，returnB

##### 3.scheduleWithFixedDelay（）方法实现周期性执行

```
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RunMain {
    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        System.out.println("          x = " + System.currentTimeMillis());
        executorService.scheduleWithFixedDelay(new MyRunable(), 1, 2, TimeUnit.SECONDS);
        System.out.println("          y = " + System.currentTimeMillis());
    }

    static class MyRunable implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println("     begin = " + System.currentTimeMillis() + ", name: " + Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(4);
                System.out.println("     end = " + System.currentTimeMillis() + ", name: " + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

结果：x=?,y=?,begin, end