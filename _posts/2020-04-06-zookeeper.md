---
layout: post
title: "zookeeper"
description: ""
category: [java,zookeeper]
tags: [zookeeper]
---
{% include JB/setup %}



# zookeeper

简介：分布式服务协同系统，集中管理不太容易控制的分布式服务，组成一个高效可靠的集群服务

场景：配置服务，命名服务，集群管理，分布式锁，分布式队列

集群结构

* 集群服务通过zk客户端连接到zk服务器
* 建立的连接是长连接

![企业微信截图_726bf2af-b91f-4ef4-8402-94d80688d681.png](http://ww1.sinaimg.cn/mw690/87a42753ly1gdjx6cr4qvj20wo0fe77y.jpg)

### zookeeper基础模块

* 文件系统

  * 和linux的文件系统类似，都是树状结构

  * 区别就是zk没有目录和文件的区别，统一称为znode，也称为节点

    * 节点类型

      * 持久型（客户端断开保留）
      * 临时型（客户端断开删除，不可以有子节点）
      * 持久顺序型（客户端断开保留）
      * 临时顺序型（客户端断开删除）（需要挂在持久节点下面）

      顺序型节点由zk维护，单项顺序递增不重复

      ![企业微信截图_c0c9c49e-0502-4284-953e-7c13071b9837.png](http://ww1.sinaimg.cn/mw690/87a42753ly1gdjxj8d1wvj20sg0mq783.jpg)

* 注册监听通知机制

  client监听zk的znode，znode有变化zk通知client（环形）

  客户端在zk上监听某个znode

  * znode是否存在（被监听的znode创建，删除）
  * znode数据是否有变化（被监听的znode数据有变化）
  * znode的子节点有没有变化（被监听的znode子节点有变化）

  ![企业微信截图_0d1bec21-ac2c-4620-9cc3-f7454ec62365.png](http://ww1.sinaimg.cn/mw690/87a42753ly1gdjxqlguwjj20x40m0n2z.jpg)

### zookeeper的常用命令



### zookeeper三种角色

* Leader
* Follower
* Observer

### zookeeper分布式锁

zookeeper的四种节点类型 

1、持久化节点 ：所谓持久节点，是指在节点创建后，就一直存在，直到有删除操作来主动清除这个节点——不会因为创建该节点的客户端会话失效而消失。

2、持久化顺序节点：这类节点的基本特性和上面的节点类型是一致的。额外的特性是，在ZK中，每个父节点会为他的第一级子节点维护一份时序，会记录每个子节点创建的先后顺序。基于这个特性，在创建子节点的时候，可以设置这个属性，那么在创建节点过程中，ZK会自动为给定节点名加上一个数字后缀，作为新的节点名。这个数字后缀的范围是整型的最大值。基于持久顺序节点原理的经典应用-分布式唯一ID生成器。

3、临时节点：和持久节点不同的是，临时节点的生命周期和客户端会话绑定。也就是说，如果客户端会话失效，那么这个节点就会自动被清除掉。注意，这里提到的是会话失效，而非连接断开。另外，在临时节点下面不能创建子节点，集群zk环境下，同一个路径的临时节点只能成功创建一个，利用这个特性可以用来实现master-slave选举。

4、临时顺序节点：相对于临时节点而言，临时顺序节点比临时节点多了个有序，也就是说每创建一个节点都会加上节点对应的序号，先创建成功，序号越小。其经典应用场景为实现分布式锁。

监视器（watcher）
当zookeeper创建一个节点时，会注册一个该节点的监视器，当节点状态发生改变时，watch会被触发，zooKeeper将会向客户端发送一条通知（就一条，因为watch只能被触发一次）。

原理
Curator内部是通过InterProcessMutex（可重入锁）来在zookeeper中创建临时有序节点实现的，之前说过，如果通过临时节点及watch机制实现锁的话，这种方式存在一个比较大的问题：所有取锁失败的进程都在等待、监听创建的节点释放，很容易发生"羊群效应"，zookeeper的压力是比较大的，而临时有序节点就很好的避免了这个问题，Curator内部就是创建的临时有序节点。

基本原理：

创建临时有序节点，每个线程均能创建节点成功，但是其序号不同，只有序号最小的可以拥有锁，其它线程只需要监听比自己序号小的节点状态即可

基本思路如下：

1、在你指定的节点下创建一个锁目录lock；

2、线程X进来获取锁在lock目录下，并创建临时有序节点；

3、线程X获取lock目录下所有子节点，并获取比自己小的兄弟节点，如果不存在比自己小的节点，说明当前线程序号最小，顺利获取锁；

4、此时线程Y进来创建临时节点并获取兄弟节点 ，判断自己是否为最小序号节点，发现不是，于是设置监听（watch）比自己小的节点（这里是为了发生上面说的羊群效应）；

5、线程X执行完逻辑，删除自己的节点，线程Y监听到节点有变化，进一步判断自己是已经是最小节点，顺利获取锁。

```
<dependency>
   <groupId>org.apache.curator</groupId>
   <artifactId>curator-recipes</artifactId>
   <version>2.4.1</version>
</dependency>
```

```
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
  
import java.util.concurrent.TimeUnit;
  
/**
 * classname：DistributedLock
 * desc：基于zookeeper的开源客户端Cruator实现分布式锁
 * author：simonsfan
 */
public class DistributedLock {
    public static Logger log = LoggerFactory.getLogger(DistributedLock.class);
    private InterProcessMutex interProcessMutex;  //可重入排它锁
    private String lockName;  //竞争资源标志
    private String root = "/distributed/lock/";//根节点
    private static CuratorFramework curatorFramework;
    private static String ZK_URL = "zookeeper1.tq.master.cn:2181,zookeeper3.tq.master.cn:2181,zookeeper2.tq.master.cn:2181,zookeeper4.tq.master.cn:2181,zookeeper5.tq.master.cn:2181";
    static{
        curatorFramework= CuratorFrameworkFactory.newClient(ZK_URL,new ExponentialBackoffRetry(1000,3));
        curatorFramework.start();
    }
     /**
     * 实例化
     * @param lockName
     */
    public DistributedLock(String lockName){
        try {
            this.lockName = lockName;
            interProcessMutex = new InterProcessMutex(curatorFramework, root + lockName);
        }catch (Exception e){
            log.error("initial InterProcessMutex exception="+e);
        }
    }
/**
     * 获取锁
     */
    public void acquireLock(){
        int flag = 0;
        try {
            //重试2次，每次最大等待2s，也就是最大等待4s
            while (!interProcessMutex.acquire(2, TimeUnit.SECONDS)){
                flag++;
                if(flag>1){  //重试两次
                    break;
                }
            }
        } catch (Exception e) {
           log.error("distributed lock acquire exception="+e);
        }
         if(flag>1){
              log.info("Thread:"+Thread.currentThread().getId()+" acquire distributed lock  busy");
         }else{
             log.info("Thread:"+Thread.currentThread().getId()+" acquire distributed lock  success");
         }
    }
     /**
     * 释放锁
     */
    public void releaseLock(){
        try {
        if(interProcessMutex != null && interProcessMutex.isAcquiredInThisProcess()){
            interProcessMutex.release();
            curatorFramework.delete().inBackground().forPath(root+lockName);
            log.info("Thread:"+Thread.currentThread().getId()+" release distributed lock  success");
        }
        }catch (Exception e){
            log.info("Thread:"+Thread.currentThread().getId()+" release distributed lock  exception="+e);
        }
    }
}
```

业务层使用时要记得释放锁。要特别注意的是 interProcessMutex.acquire(2, TimeUnit.SECONDS)方法，可以设定等待时候，加上重试的次数，即排队等待时间= acquire × 次数，这两个值一定要设置好，因为使用了分布式锁之后，接口的TPS就下降了，没获取到锁的接口都在等待/重试，如果这里设置的最大等待时间4s，这时并发进来1000个请求，4秒内处理不完1000个请求怎么办呢？所以一定要设置好这个重试次数及单次等待时间，根据自己的压测接口设置合理的阈值，避免业务流转发生问题！