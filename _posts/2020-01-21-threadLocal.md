用处

每个线程往ThreadLocal中读写数据都是线程隔离，互相之间不会影响的，所以ThreadLocal无法解决共享对象的更新问题。由于不需要共享信息，自然就不存在竞争关系，从而保证了某些情况下线程的安全，以及避免了某些情况需要考虑线程安全必须带来的性能损失。

![1579597854220.jpg](http://ww1.sinaimg.cn/large/87a42753ly1gb4asqwcqbj20tf0m4tb8.jpg)

Entry就相当于Pair，key是一个弱引用对象WeakReference<ThreadLocal>

弱引用：描述非必需对象的，当JVM进行垃圾回收时，无论内存是否充足，该对象仅仅被弱引用关联，那么就会回收。

此时Entry的key虽然被回收了，但是Entry还是一个强引用对象，里面还存储Object，ThreadLocalMap做了一些额外的回收工作,`expungeStaleEntry`，据说会存在内存泄漏，没碰见过

 

