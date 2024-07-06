---
layout: post
title: "cloud-ribbon"
description: ""
category: [cloud]
tags: [cloud]
---
{% include JB/setup %}


### 组件
* rule
* ping
* loadBalancer

### 大致流程
* 找到负载均衡注解@LoadBalanced，找到自动配置化的类和类里面比较重要的组成部分，心里先有个大概印象
* 针对被负载均衡注解修饰的restTemplate增加一个负载均衡的过滤器loadBalancedInterceptor
* 通过负载均衡的过滤器找到负载均衡器，找到具体的服务实例

* feign通过@LoadBalanced注解可以达到负载均衡的效果，在restTemplate对象上面放上注解即可
* 找到@LoadBalanced注解的位置，同包下找到LoadBalancerAutoConfiguration，或者通过spring.factories里面找[给所有restTemplate注入拦截器]
  * 初始化loadBalancedInterceptor拦截器 
  * 初始化restTemplateCustomizer（定制入参的resttemplate），拿到入参resttemplate传进来的所有拦截器，顺便把上一步初始化的loadbalancedInterceptor拦截器加上，重新放到resttemplate里面
  * 初始化smartInitializingSingleton，拿到代码里面所有被注解@loadBalanced标记的restTemplate，组成restTemplateList，依次执行上一步，也就是对resttemplate对象进行拦截器的赋值
* restTemplate.forObject("http://stock-service/stock/getStockNum",String.class);执行方法发起请求
  * 先走到上面给restTemplate增强的拦截器loadbalancedInterceptor里面，拿到uri，通过uri拿到host[serviceName]，也就是业务系统的标识stock-service
  * 执行LoadBalancerClient.execute方法，把serviceName,request,body当成入参传进去[肯定是对服务名解析，拿到ip和端口，进行负载均衡]
* 执行LoadBalancerClient.execute方法
  * ILoadBalancer loadBalancer=getLoadBalancer(serviceId);[获取负载均衡器]
    * 从容器里面拿一个ILoadBalancer的实现类[问题1:就是在哪里初始化了之后放到容器里面的]
  * Server server=getServer(loadBalancer,hint);[负载均衡器根据负载均衡算法获取一个server]
    * 调用loadBalancer.chooseServer() 方法，也就走到了ZoneAwareLoadBalancer类的chooseServer方法 
    * 4.2[给gateway里面标记4.2的一个位置用] 调用rule.choose方法 [问题2:rule的是实现类]
    * 走到predicateBaseRule中的choose方法，里面是chooseRoundRobinAfterFiltering(loadbalancer的所有实例地址，微服务标识)[问题3:loadbalancer的所有实例地址怎么来的]
    * 从总server里面过滤出来可用server。之后根据可用server去选择具体的server，选择的方式是incrementAndGetModulo(可用server大小)，轮询选出来server




### 问题1
* RibbonClientConfiguration这个类是初始化的类，里面的方法public ILoadBalancer ribbonLoadBalancer(){}是把负载均衡器放容器里面的实现。
* 实际的负载均衡器是ZoneAwareLoadBalancer
### 问题2
* 三个组件rule，ping，loadbalancer初始化肯定写到一起了，RibbonClientConfiguration找到ribbonRule和ribbonPing方法
* ribbonRule如果配置文件没有配置负载均衡算法，其实执行的就是轮询算法去获取server（配置文件的优先级高于轮询），new的对象虽然是zoneAvoidanceRule，但是真正的实现是predicateBaseRule（zoneAvoidanceRule的父类 ）
* 实际的rule实现是predicateBaseRule
### 问题3
* 
