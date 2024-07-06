---
layout: post
title: "cloud-feign"
description: ""
category: [cloud]
tags: [cloud]
---
{% include JB/setup %}


### 组件
* ribbon
* restTemplate

### 大致流程
* spring那套都是先扫描，找到，注册，放容器里面，之后从容器里面取出来@feignclient对象，通过动态代理加强，把请求参数拼装好
* 之后通过ribbon替换成ip+端口发起请求

### 细节 [动态代理]
* 启动类注解@EnableFeignClient，注解是入口，找到了enableFeignAutoConfiguration，里面就是一些初始化的操作，没什么太多的重点，主要是@EnableFeignClient中声明的一个配置类FeignClientRegister。
* FeignClientRegister里面的重点是registerBeanDefinitions方法（扫描 被@Feignclient注解配置的类）
* 将上一步的@feignClient类调用loadBalancer方法，通过代理模式生成出来loadBalanceFeignClient代理类放到容器中
* 请求真正执行的时候走的是loadBalanceFeignClient.execute方法
* 创建FeignLoadBalancer.RibbonRequest ribbonRequest对象，此时和ribbon打通了，替换请求里面的服务名，完成请求



 