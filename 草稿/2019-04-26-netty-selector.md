---
layout: post
title: "netty-selector"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

# Netty-selector

### 为什么使用Selector

Selector（选择器）是Java NIO中能够检测一到多个NIO通道，并能够知晓通道是否为诸如读写事件做好准备的组件。这样，一个单独的线程可以管理多个channel，从而管理多个网络连接。

### Selector创建

```java
Selector selector = Selector.open();
```

### 向Selector注册通道

为了将channel和selector配合使用，需要将channel注册到selector上

```java
channel.configureBlocking(false);
SelectionKey key = channel.register(selector,Selectionkey.OP_READ);
```

>  与selector一起使用时，channel必须处于非阻塞状态

> register()方法的第二个参数。这是一个“interest集合”，意思是在通过Selector监听Channel时对什么事件感兴趣。
>
> 可以监听四种不同类型的事件
>
> 1.connect
>
> 2.accept
>
> 3.read
>
> 4.write
>
> 通道触发了一个事件意思就是该事件已经就绪
>
> 某个channel成功连接到另一个服务器称为-连接就绪
>
> 一个server socket channel准备好接收新进入的连接称为-接收就绪
>
> 一个有数据可读的通道可以说是-读就绪
>
> 等待写数据的通道可以说是-写就绪
>
> 如果对不止一种事件感兴趣，可以用位或操作符将常量连接起来
>
> ```java
> int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
> ```

### SelectionKey

> interest集合-感兴趣的集合事件[selectionKey.interestOps()]
>
> ready集合-已经就绪的操作集合[selectionKey.readyOps()]
>
> Channel[selectionKey.channel()]
>
> Selector[selectionKey.selector()]
>
> 附加的对象（可选）

### 通过Selector选择通道

一旦向Selector注册了一或多个通道，就可以调用几个重载的select()方法。这些方法返回你所感兴趣的事件（如连接、接受、读或写）已经准备就绪的那些通道。

> `select()`返回的int值表示有多少通道已经就绪。
>
> `select(long timeout)`和select()一样，除了最长会阻塞timeout毫秒(参数)。
>
> `selectNow()`不会阻塞，不管什么通道就绪都立刻返回

### WakeUp

某个线程调用select()方法后阻塞了，即使没有通道已经就绪，也有办法让其从select()方法返回。只要让其它线程在第一个线程调用select()方法的那个对象上调用Selector.wakeup()方法即可。阻塞在select()方法上的线程会立马返回。

如果有其它线程调用了wakeup()方法，但当前没有线程阻塞在select()方法上，下个调用select()方法的线程会立即“醒来（wake up）”。

### close

用完Selector后调用其close()方法会关闭该Selector，且使注册到该Selector上的所有SelectionKey实例无效。通道本身并不会关闭

### 示例

selector对注册的channel轮询访问，一单轮询到一个channel有所注册的时间发生，就会报告，交出来一把钥匙，通过钥匙来读取这个channel的内容

```java
//使用selector
Selector selector = Selector.open();
//建立Channel 并绑定到9000端口
ServerSocketChannel channel = ServerSocketChannel.open();
InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(),9000); 
channel.socket().bind(address);
//设定non-blocking方式
channel.configureBlocking(false);
//向selector注册channel以及我们有兴趣的事件
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
while(true) {
  //selector通过select方法通知我们，我们感兴趣的事件发生了
  int readyChannels = selector.select();
  //如果有我们感兴趣的事件，readyChannels大于0
  if(readyChannels == 0) continue;
  //selector传回一组selectedKeys，我们从这些key的channel方法取得我们注册的channel
  Set selectedKeys = selector.selectedKeys();
  Iterator keyIterator = selectedKeys.iterator();
  while(keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();
    if(key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
    } else if (key.isConnectable()) {
        // a connection was established with a remote server.
    } else if (key.isReadable()) {
        // a channel is ready for reading
    } else if (key.isWritable()) {
        // a channel is ready for writing
    }
    keyIterator.remove();
  }
}
```