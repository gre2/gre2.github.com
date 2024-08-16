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

#### 问题1：ThreadLocal是什么，为什么使用它

并发场景下，会存在多个线程同时修改一个共享变量的场景。这就可能会出现线性安全问题。ThreadLocal起到线程隔离的作用，避免了并发场景下的线程安全问题。
使用ThreadLocal类访问共享变量时，会在每个线程的本地，都保存一份共享变量的拷贝副本。多线程对共享变量修改时，实际上操作的是这个变量副本，从而保证线性安全。

#### 问题2：key是弱引用，GC回收会影响ThreadLocal的正常工作嘛？
其实不会的，因为有ThreadLocal变量引用着它，是不会被GC回收的，除非手动把ThreadLocal变量设置为null，我们可以跑个demo来验证一下：
```java

public class WeakReferenceTest {
    public static void main(String[] args) {
        Object object = new Object();
        WeakReference<Object> testWeakReference = new WeakReference<>(object);
        System.out.println("GC回收之前，弱引用："+testWeakReference.get());
        //触发系统垃圾回收
        System.gc();
        System.out.println("GC回收之后，弱引用："+testWeakReference.get());
        //手动设置为object对象为null
        object=null;
        System.gc();
        System.out.println("对象object设置为null，GC回收之后，弱引用："+testWeakReference.get());
    }
}
运行结果：
GC回收之前，弱引用：java.lang.Object@7b23ec81
GC回收之后，弱引用：java.lang.Object@7b23ec81
对象object设置为null，GC回收之后，弱引用：null
```

内存泄露的例子
```java
public class ThreadLocalTestDemo {
 
    private static ThreadLocal<TianLuoClass> tianLuoThreadLocal = new ThreadLocal<>();
 
 
    public static void main(String[] args) throws InterruptedException {
 
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
 
        for (int i = 0; i < 10; ++i) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("创建对象：");
                    TianLuoClass tianLuoClass = new TianLuoClass();
                    tianLuoThreadLocal.set(tianLuoClass);
                    tianLuoClass = null; //将对象设置为 null，表示此对象不在使用了
                   // tianLuoThreadLocal.remove();
                }
            });
            Thread.sleep(1000);
        }
    }
 
    static class TianLuoClass {
        // 100M
        private byte[] bytes = new byte[100 * 1024 * 1024];
    }
}
 
 
创建对象：
创建对象：
创建对象：
创建对象：
Exception in thread "pool-1-thread-4" java.lang.OutOfMemoryError: Java heap space
 at com.example.dto.ThreadLocalTestDemo$TianLuoClass.<init>(ThreadLocalTestDemo.java:33)
 at com.example.dto.ThreadLocalTestDemo$1.run(ThreadLocalTestDemo.java:21)
 at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
 at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
 at java.lang.Thread.run(Thread.java:748)
```

#### 问题3：ThreadLocal的Key为什么要设计成弱引用呢？

其实我感觉没啥用，如果不执行set get remove依然存在内存溢出的问题，可是反之如果都执行这些命令了，那我直接强引用执行=null不也可以吗？或者用软引用也没啥不可以的啊。

#### 问题4： InheritableThreadLocal保证父子线程间的共享数据
```java
public class InheritableThreadLocalTest {
 
   public static void main(String[] args) {
       ThreadLocal<String> threadLocal = new ThreadLocal<>();
       InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
 
       threadLocal.set("关注公众号：捡田螺的小男孩");
       inheritableThreadLocal.set("关注公众号：程序员田螺");
 
       Thread thread = new Thread(()->{
           System.out.println("ThreadLocal value " + threadLocal.get());
           System.out.println("InheritableThreadLocal value " + inheritableThreadLocal.get());
       });
       thread.start();
       
   }
}
//运行结果
ThreadLocal value null
InheritableThreadLocal value 关注公众号：程序员田螺
```

如果当前线程的inheritableThreadLocals不为null，就从父线程哪里拷贝过来一个过来，类似于另外一个ThreadLocal，数据从父线程那里来的。



 

