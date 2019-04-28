---
layout: post
title: "netty-buffer"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

## Netty-buffer

### buffer介绍

buffer本质是一块可以写入数据，然后可以从中读取数据的内存，这块内存被包装成nio bufferr对象，并提供一组方法，用来方便的访问这块内存

buffer用于和channel进行交互，数据从channel读入buffer，从buffer写进channel

### buffer的三个属性

capacity，position，limit

![image](https://wx4.sinaimg.cn/mw690/87a42753ly1g2ics25tmcj20tm0j60us.jpg)

> * buffer作为内存块，有一个固定的大小值capacity，你只能往里面写capacity个byte，long，char等类型
> * 写数据到buffer时，position表示当前位置，写入数据后，position会向前移动到下一个可插入数据的buffer单元，最大是capacity-1
> * 读数据时，也是从某个特定位置读，buffer模式切换，position被重置为0，当从buffer的position处读数据时，position向前移动到下一个可读的位置
> * 写模式下limit等于buffer的capacity，表示能往buffer写多少数据
> * 读模式下，limit表示最多能读到多少数据因此，当切换Buffer到读模式时，limit会被设置成写模式下的position值。换句话说，你能读到之前写入的所有数据（limit被设置成已写数据的数量，这个值在写模式下就是position）

### buffer类型

- ByteBuffer
- MappedByteBuffer
- CharBuffer
- DoubleBuffer
- FloatBuffer
- IntBuffer
- LongBuffer
- ShortBuffer

### buffer分配

要想获得一个Buffer对象首先要进行分配。 每一个Buffer类都有一个allocate方法。下面是一个分配48字节capacity的ByteBuffer的例子。

```java
ByteBuffer buf = ByteBuffer.allocate(48);
```

### 向buffer写数据

* 从channel写到buffer

```java
int bytesRead = inChannel.read(buf);
```

* 通过buffer的put方法写到buffer里

```java
buf.put(127);
```

### flip方法

将buffer从写模式切换成读模式，本质是更改position和limit的值

### 从buffer读数据

* 从buffer读取数据到channel

```java
int bytesWritten = inChannel.write(buf);
```

* 使用get方法从buffer读取数据

```java
`byte` `aByte = buf.get();`
```

### rewind方法

将position设为0，可以重新读buffer中的所有数据，limit保持不变

### clear()与compact()

一旦读完Buffer中的数据，需要让Buffer准备好再次被写入。可以通过clear()或compact()方法来完成。

* 如果调用的是clear()方法，position将被设回0，limit被设置成 capacity的值。换句话说，Buffer 被清空了。Buffer中的数据并未清除，只是这些标记告诉我们可以从哪里开始往Buffer里写数据。

  > 如果Buffer中有一些未读的数据，调用clear()方法，数据将“被遗忘”，意味着不再有任何标记会告诉你哪些数据被读过，哪些还没有。

* compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。limit属性依然像clear()方法一样，设置成capacity。现在Buffer准备好写数据了，但是不会覆盖未读的数据。

### mark()与reset()

通过调用Buffer.mark()方法，可以标记Buffer中的一个特定position。之后可以通过调用Buffer.reset()方法恢复到这个position。

