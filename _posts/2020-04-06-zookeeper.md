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

