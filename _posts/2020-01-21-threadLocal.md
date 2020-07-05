用处

每个线程往ThreadLocal中读写数据都是线程隔离，互相之间不会影响的，所以ThreadLocal无法解决共享对象的更新问题。由于不需要共享信息，自然就不存在竞争关系，从而保证了某些情况下线程的安全，以及避免了某些情况需要考虑线程安全必须带来的性能损失。

![1579597854220.jpg](http://ww1.sinaimg.cn/large/87a42753ly1gb4asqwcqbj20tf0m4tb8.jpg)

Entry就相当于Pair，key是一个弱引用对象WeakReference<ThreadLocal>

set的时候创建一个数组16位，三分之二容量扩容2倍，通过threadLocalHashCode & (len-1)计算下标，通过线性探测的方式解决hash冲突，如果发现这个位置上已经有其他key值的元素被占用，则理由固定的算法寻找一定步长的下个位置，依次判断，知道找到能够存放的位置（ThreadLocalMap解决Hash冲突的方式就是简单的步长加1或减1，寻找下一个相邻的位置。）

```
/**
 * Increment i modulo len.
 */
private static int nextIndex(int i, int len) {
    return ((i + 1 < len) ? i + 1 : 0);
}

/**
 * Decrement i modulo len.
 */
private static int prevIndex(int i, int len) {
    return ((i - 1 >= 0) ? i - 1 : len - 1);
}
```

显然ThreadLocalMap采用线性探测的方式解决Hash冲突的效率很低，如果有大量不同的ThreadLocal对象放入map中时发送冲突，或者发生二次冲突，则效率很低。

所以这里引出的良好建议是：每个线程只存一个变量，这样的话所有的线程存放到map中的Key都是相同的ThreadLocal，如果一个线程要保存多个变量，就需要创建多个ThreadLocal，多个ThreadLocal放入Map中时会极大的增加Hash冲突的可能。


实际上 ThreadLocalMap 中使用的 key 为 ThreadLocal 的弱引用，弱引用的特点是，如果这个对象只存在弱引用，那么在下一次垃圾回收的时候必然会被清理掉。

所以如果 ThreadLocal 没有被外部强引用的情况下，在垃圾回收的时候会被清理掉的，这样一来 ThreadLocalMap中使用这个 ThreadLocal 的 key 也会被清理掉。但是，value 是强引用，不会被清理，这样一来就会出现 key 为 null 的 value。

ThreadLocalMap实现中已经考虑了这种情况，在调用 set()、get()、remove() 方法的时候，会清理掉 key 为 null 的记录。如果说会出现内存泄漏，那只有在出现了 key 为 null 的记录后，没有手动调用 remove() 方法，并且之后也不再调用 get()、set()、remove() 方法的情况下。

 

