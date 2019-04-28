---
layout: post
title: "rocketmq-namserv"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

# Rocketmq-namserv

### 特点

高可用，由于各个nameserver之间并无通信，故一个namerserver挂了并不会影响其他namerserver

### 实现

* 初始化namservController，包含namesrvConfig（namesrv相关配置），nettyServerConfig（netty的相关配置），KVConfigManager（KV配置管理），RouteInfoManager（路由信息、topic信息管理），BrokerHousekeepingService（broker管理服务）
* NamesrvController.initialize()：加载KV配置，初始化通讯层（Netty的初始化：remotingServer对象），初始化线程池remotingExecutor，向remotingServer对象中注册DefaultRequestProcessor对象
* 启动定时扫描notActive的broker任务；启动定时将configTable相关信息记录到日志文件中任务

### 数据

![image](https://ws1.sinaimg.cn/small/87a42753ly1g25vfvlj61j20di09eq3r.jpg)

### 类属性

##### KVConfigManager#configTable

> ```java
> HashMap<String/* Namespace */, HashMap<String/* Key */, String/* Value */>> 
> ```

##### RouteInfoManager#topicQueueTable

> ```java
> HashMap<String/* topic */, List<QueueData>>
> ```
>
> ```java
> public class QueueData implements Comparable<QueueData> {
>     /** Broker名*/
>     private String brokerName;
>     /**读队列长度*/
>     private int readQueueNums;
>     /**写队列长度*/
>     private int writeQueueNums;
>     /** 读写权限*/
>     private int perm;
>     private int topicSynFlag;
> }
> ```

##### RouteInfoManager#brokerAddrTable

> ```java
> HashMap<String/* brokerName */, BrokerData>
> ```
>
> ```java
> public class BrokerData implements Comparable<BrokerData> {
>     /**集群名*/
>     private String cluster;
>     /**Broker名*/
>     private String brokerName;
>     /**broker角色编号 和 broker地址 Map*/
>     private HashMap<Long/* brokerId */, String/* broker address */> brokerAddrs;
> }
> ```

##### RouteInfoManager#clusterAddrTable

> ```java
> HashMap<String/* clusterName */, Set<String/* brokerName */>>
> ```

##### RouteInfoManager#brokerLiveTable  [broker地址与broker连接信息]

> ```java
> HashMap<String/* brokerAddr */, BrokerLiveInfo>
> ```
>
> ```java
> class BrokerLiveInfo {
>     /**最后更新时间*/
>     private long lastUpdateTimestamp;
>     /**数据版本号*/
>     private DataVersion dataVersion;
>     /**连接信息*/
>     private Channel channel;
>     /**ha服务器地址*/
>     private String haServerAddr;
> }
> ```

##### RouteInfoManager#filterServerTable  [broker地址 与 filtersrv数组 Map(用于类模式消息过滤)]

> ```java
> HashMap<String/* brokerAddr */, List<String>/* Filter Server */>
> ```

### namserv接收到broker的注册请求

org.apache.rocketmq.namesrv.processor.DefaultRequestProcessor#processRequest

> 如果topic配置信息发生变更或者是第一次为该broker注册，根据brokername及topicconfig（read、write queue数量等）新增或者更新到topicQueueTable中
>
> ```java
> private final HashMap<String/* topic */, List<QueueData>> topicQueueTable;
> ```

- 将当前请求注册的broker信息保存或者更新到clusterAddrTable，brokerAddrTable中
- 将当前请求注册的broker的topic信息保存或者更新到topicQueueTable中

broker定时上报，namserv定时更新