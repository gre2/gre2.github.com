---
layout: post
title: "cloud-gateway"
description: ""
category: [cloud]
tags: [cloud]
---
{% include JB/setup %}

### 组件
* 断言==找路由
* 过滤器==执行网关的加强逻辑

### 大致流程
* 看自动配置类和自动配置类里面比较重要的组成部分，心里先有个大概印象
* 请求过来之后，从yaml拿到所有的route路由，根据断言找到真正的路由
* 找到全局过滤器和自己本身的过滤器，变成链式，主要功能会在过滤器里面实现
* 走负载均衡过滤器LoadBalancerClientFilter，把微服务标识转换成ip和端口[和ribbon打通了]
* 走别的过滤器逻辑
* 走发送请求的过滤器nettyRouteFilter，真正的发送请求

### 细节 [里面ribbon初始化在ribbon模块说]
* 初始化流程：spring-cloud-gateway-core.jar里面spring.factories里面的自动加载类[gatewayAutoConfiguration]
* 核心：gatewayLoadBalancerAutoConfiguration（负载均衡相关）+gatewayAutoConfiguration（网关核心配置里面包含几个重要的东西）
  * gatewayLoadBalancerAutoConfiguration组成：
    * @bean初始化路由负载均衡过滤器LoadBalancerClientFilter[执行invokeHandler方法中过滤器链的时候会执行],这个类实现了全局过滤器接口GlobalFilter
  * gatewayAutoConfiguration组成： 
    * @bean查找匹配到route并进行处理RoutePrdicateHandlerMapping（重要 ）[下面mapping.getHandler找路由的实现类]
    * @bean加载网关配置gatewayProperties[yaml文件读取类]
    * @bean创建一个根据routedefition转换的路由定位器RouteDefinitionRouteLocator[路由类]
    
```
spring:
     cloud:
       gateway:
         routes:
         - id: user-service-route
           uri: lb://user-service
           predicates:
           - Path=/api/users/**
           filters:
           # 为原始请求添加名为X-Request-Foo，值为Bar的请求头信息
           - AddRequestHeader=X-Request-Foo,Bar
```

* 请求过来打到网关[读取yaml组成routes，筛选得到route]
* DispatcherHandler.handle方法（DispatcherHandler类似mvc中的dispatherServlet对象）
* 走到mapping.getHandler方法，走到实现类RoutePrdicateHandlerMapping.getHandlerInternal方法 ，里面执行lookupRoute方法[把配置文件里面的数据映射成route路由，放到httpServletRequest里面]
  * 走到RouteDefinitionRouteLocator.getRoutes方法，之后找到真正的实现类PropertiesRouteDefinitionLocator.getRoutes方法，内部实现是[gatewayProperties类]的成员变量（程序启动的时把网关配置文件里面的内容放到了类对象中）
，把里面的内容映射成routeDefination[predicates,filter这些配置文件里面的属性值]，转换routeDefination变成route 
  * 根据请求找到真正的路由route，放到exchange里面（类似http里面的httpServletRequest，反正就是一个请求的全局变量）
* 执行invokeHandler方法[拿到过滤器，先负载，之后执行自定义过滤器逻辑，之后发起请求]
  * 拿到上面的路由route，拿到路由里面的filters，拿到全局过滤器[globalFilters]，把route里面的过滤器和全局过滤器合并变成过滤器链combined。
  * new DefaultGatewayFilterChain(combined).filter(exchange);[会执行gateway初始化的时候加载的负载均衡过滤器链LoadBalancerClientFilter]，依次执行每个过滤器。
  * 先执行LoadBalancerClientFilter.filter方法[负载均衡过滤器]
    * ServiceInstance instance=choose(exchange); 
      * 拿到route里面的uri，(lb://stock-service）[lb代表从注册中心获取服务，stock-service代表微服务名称，也就是路由的唯一标识 ]。
      * 拿到服务标识之后找到choose的实现类ribbonLoadBalancerClient。[和ribbon打通了] [被nacos的包引入的ribbon]
      * [ribbon初始化，看ribbon源码，从容器里面获取loadBalancer]和ribbon实现一样，初始化的时候放容器里面的，是ribbon里面的4.1.其实拿到的对象是父类对象DynamicServerListLoadBalancer  （ZoneAwareLoadBalancer的父类）。此时这个类的初始化其实和nacos有交互，拿nacos的实例列表 .走到ribbon中的4.2。
    * 在gateway中拿到ribbon实例对象类名叫ServerInstance.这个对象其实就是我们真正微服务的实例信息。拿到这个对象的目的是为了把访问网关的url进行替换，除了uri部分替换成真正微服务的实例地址信息
        举例：网关8080端口，19机器。微服务9091端口20机器，访问http://19:8080/ads转成http://20:9091/ads
  * 之后会走完每个过滤器的业务逻辑
  * 最后发起请求[也是过滤器中的一个全局过滤器，为什么可以最后执行，前面串过滤器链的时候按照order排序了]，真正发起请求的类是nettyRouteFilter.filter ，逻辑其实就是httpclient走网络请求。

### gateway应用[全都是filter的应用]
* 自定义filter
  * 写代码，实现全局globalFilter，写逻辑。不容易扩展
  * 写代码继承AbstractGatewayFilterFactory，可以设置成根据path配置的，也可以设置成所有path的
* 探活[还是filter]
  * ```
       - id :gateway-status
       uri: lb://abc
       predicates:
           - Path=/api/gateway/status
       filters:
         - CheckStatus
    ```
    
  * ```java
       @Service
       public class CheckStatusGatewayFilterFactory extends AbstractGatewayFilterFactory {
         @Override
         public GatewayFilter apply(Object config){
              return (exchange,chain)->{
                     log. Info("gateway filter uri:{}", exchange.getRequest().getPath());
                     exchange.getResponse().setStatusCode (HttpStatus.OK);
                     DataBufferFactory dataBufferfactory = exchange.getResponse().bufferfactory();
                     DataBuffer wrap = dataBufferFactory.wrap("ok".getBytes (StandardCharsets.UTF_8));
                     return exchange.getResponse().writeWith(Mono.just(wrap));
              };
         }
       }
    ```
    
* 登录和鉴权
  * 所有功能都类似，就是自定义filter之后改下配置，业务操作，传递数据可以用exchange，链式调用exhcange走不同的过滤器
* 平滑切换服务（灰度）
  * 两套路由，用权重weight断言引流
* 限流
  * 见限流文章的gateway部分
* 安全网关签名验证
  * 验证访问者身份（请求头信息，请求头里面有时间戳，按照字符串拼接不同的请求头之后配合密钥加密，加密后的密钥也在请求头里面，比较）
  * 验证内容是否被篡改(不推荐验证这个)

 