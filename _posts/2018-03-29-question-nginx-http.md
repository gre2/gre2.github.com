---
layout: post
title: "nginx日志有请求数据，resin（业务系统）无日志"
description: ""
category: [java,http]
tags: [question]
---
{% include JB/setup %}


## 线上问题积累

 	1.description

      接到端上的报警，说是499请求超时，第一个想法就是看看resin日志，但是居然没有；紧随其后看看nginx日志，有；
     
        此时问题很明确了，直接观察nginx日志的调用请求，上图

        ![](https://ws1.sinaimg.cn/large/87a42753ly1fptxxm5xfcj20zy01s74i.jpg)
    
    2.分析

	 从10.110请求到10.11是499的共性，超时时间3.000，代表3s，返回地址499，resin无日志，很明显nginx和resin交互失败；

     直接找网络组的人问了一下两个机器处于什么机房，之间的通讯是否有问题；

     收到的答复是：跨机房，而且机房之间通讯有异常今天，导致499；


    3.解决

      修改机房配置，内网调用，不进行跨机房操作，当然最主要是保证每个机房机器的联通；


    4.弯路

      因为resin没有日志，所以不要去看什么jvm，数据库死锁，索引之类的，这些只会导致resin有日志，但是返回超时，千万抓住问题的本质
	
	  