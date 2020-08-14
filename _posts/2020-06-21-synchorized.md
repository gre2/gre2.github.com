---
layout: post
title: "synchorized+ReentrantLock"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}



### 线程安全点

当线程执行到这些位置的时候，说明虚拟机当前状态是安全的，如果需要，可以在这个位置暂停，比如发生GC，需要执行所有活动线程，但是该线程在这个时刻，还没有执行到安全点，所以该线程应该继续执行，到达下一个安全点的时候暂停，然后开始GC，该线程等待GC结束。

### synchorized加锁位置

* 对象
  * 对象在内存中分为三块区域（对象头，实例数据，对齐填充）
  * 操作系统层面通过Mutex
* 方法
  * 操作系统层面通过ACC_SYNCHRONIZED标识
* 代码块
  * 操作系统层面通过monitorCenter，monitorExit指令**，其中 monitorenter 指令指向同步代码块的开始位置，monitorexit 指令则指明同步代码块的结束位置。**

### 对象头Mark Words

![企业微信截图_e839b2ac-a6d6-482a-853d-ec52cce64eb0.png](http://ww1.sinaimg.cn/large/87a42753ly1gfzrtoi1kqj217m0lcdik.jpg)

另外 Monitor 中还有两个队列分别是EntryList和WaitList，主要是用来存放进入及等待获取锁的线程。

### synchorized矛盾点

* **A**: 重量级锁中的阻塞(挂起线程/恢复线程): 需要转入内核态中完成，有很大的性能影响。

  **B**: 锁大多数情况都是在很短的时间执行完成。

  **解决方案**: 引入轻量锁(通过自旋来完成锁竞争)。

* **A**: 轻量级锁中的自旋: 占用CPU时间，增加CPU的消耗(因此在多核处理器上优势更明显)。

  **B**: 如果某锁始终是被长期占用，导致自旋如果没有把握好，白白浪费CPU资源。

  **解决方案**: JDK5中引入默认自旋次数为10(用户可以通过`-XX:PreBlockSpin`进行修改)， JDK6中更是引入了自适应自旋（简单来说如果自旋成功概率高，就会允许等待更长的时间（如100次自旋），如果失败率很高，那很有可能就不做自旋，直接升级为重量级锁，实际场景中，HotSpot认为最佳时间应该是一个线程上下文切换的时间，而是否自旋以及自旋次数更是与对CPUs的负载、CPUs是否处于节电模式等息息相关的)。

* **A**: 无论是轻量级锁还是重量级锁: 在进入与退出时都要通过CAS修改对象头中的`Mark Word`来进行加锁与释放锁。

  **B**: 在一些情况下总是同一线程多次获得锁，此时第二次再重新做CAS修改对象头中的`Mark Word`这样的操作，有些多余。

  **解决方案**: JDK6引入偏向锁(首次需要通过CAS修改对象头中的`Mark Word`，之后该线程再进入只需要比较对象头中的`Mark Word`的Thread ID是否与当前的一致，如果一致说明已经取得锁，就不用再CAS了)。

* **A**: 项目中代码块中可能绝大情况下都是多线程访问。

  **B**: 每次都是先偏向锁然后过渡到轻量锁，而偏向锁能用到的又很少。

  **解决方案**: 可以使用`-XX:-UseBiasedLocking=false`禁用偏向锁。

* **A**: 代码中JDK原生或其他的工具方法中带有大量的加锁。

  **B**: 实际过程中，很有可能很多加锁是无效的(如局部变量作为锁，由于每次都是新对象新锁，所以没有意义)。

  **解决方法**: 引入锁削除(虚拟机即时编译器(JIT)运行时，依据逃逸分析的数据检测到不可能存在竞争的锁，就自动将该锁消除)。

* **A**: 为了让锁颗粒度更小，或者原生方法中带有锁，很有可能在一个频繁执行(如循环)中对同一对象加锁。

  **B**: 由于在频繁的执行中，反复的加锁和解锁，这种频繁的锁竞争带来很大的性能损耗。

  **解决方法**: 引入锁膨胀(会自动将锁的范围拓展到操作序列(如循环)外, 可以理解为将一些反复的锁合为一个锁放在它们外部)。
### synchorized锁升级

无锁–>偏向锁（等待竞争出现才释放锁的机制）–>轻量级锁–>重量级锁

* 无锁->偏向锁  [T1|epoch|1|01]
  * 线程1访问同步代码块
  * 检查`lock`对象的对象头(`mark word`)中是否存储了线程1
  * 如果是，执行同步代码块
  * 如果不是，则通过CAS替换，设置`mark word`中的`线程ID`为T1，替换成功，执行同步代码块，替换不成功，升级锁。
* 偏向锁->轻量级锁 [空|0|01]->[轻量级锁指针|00]
  * 线程2访问同步代码块
  * 检查`lock`对象的对象头，发现存储的是线程1，CAS替换失败
  * 等待线程1到达安全点，暂停线程1
  * 检查线程1的状态
    * 如果是未活动状态或者已经退出同步代码块，撤销线程1的偏向锁，重新恢复成无锁状态，唤醒线程2。
    * 如果未退出同步代码块，升级为轻量级锁，在线程1的栈中分配锁记录，拷贝对象头中的mark word到线程1的锁记录中。与此同时线程2的栈也会分配锁记录，拷贝对象头中的mark word到线程2的锁记录中。线程1获得轻量级锁 （[xxx]**此时线程2：**CAS将对象头的mark word中的锁记录指针指向线程2，如果成功，线程2获得轻量级锁，**线程1没有执行完同步代码块的时候，线程2肯定不会成功**），唤醒线程1（原从持有偏向锁的线程），从安全点继续执行同步代码块，执行完之后进行轻量级锁的解锁操作。
      * 轻量级锁的解锁操作：1.对象头中的mark word中锁记录指针是否仍指向当前线程锁记录，2.拷贝在当前线程的mark word信息是否与对象头中的mark word一致。1&&2成功，释放锁。1&&2 失败，释放锁，同时唤醒被挂起的线程，开启新一轮锁竞争。
* 轻量级锁->重量级锁 [ 重量级锁指针|10]
  * 偏向锁升级到轻量级锁的xxx的步骤，线程2，CAS自旋将对象头的mark wor中的锁记录指针指向当前线程2
    * 在一定次数范围内成功的话会获取轻量级锁，执行代码块，执行完之后释放轻量级锁
    * 但是在一定次数仍然没有成功，会升级到重量级锁，mutex挂起当前线程。

![企业微信截图_d677a6ad-830f-4cb0-a0a6-501c99bd945c.png](http://ww1.sinaimg.cn/large/87a42753ly1gfzukq7d6jj220m0y6ao0.jpg)



### ReentrantLock之AQS

* 核心思想

  **AQS核心思想是，如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的工作线程，并且将共享资源设置为锁定状态。如果被请求的共享资源被占用，那么就需要一套线程阻塞等待以及被唤醒时锁分配的机制，这个机制AQS是用CLH队列锁实现的，即将暂时获取不到锁的线程加入到队列中。**

* 原理实现

  `AQS`中 维护了一个`volatile int state`（代表共享资源）和一个`FIFO`线程等待队列（多线程争用资源被阻塞时会进入此队列）。

  这里`volatile`能够保证多线程下的可见性，当`state=1`则代表当前对象锁已经被占有，其他线程来加锁时则会失败，加锁失败的线程会被放入一个`FIFO`的等待队列中，比列会被`UNSAFE.park()`操作挂起，等待其他获取锁的线程释放锁才能够被唤醒。

  另外`state`的操作都是通过`CAS`来保证其并发修改的安全性。

* 定义两种资源共享方式

  - **Exclusive**（独占）：只有一个线程能执行，如ReentrantLock。又可分为公平锁和非公平锁：

  - - 公平锁：按照线程在队列中的排队顺序，先到者先拿到锁
    - 非公平锁：当线程要获取锁时，无视队列顺序直接去抢锁，谁抢到就是谁的

  - **Share**（共享）：多个线程可同时执行，Semaphore、CountDownLatch、 CyclicBarrier、ReadWriteLock 。

* 线程1加锁成功，线程2和线程3加锁失败（线程1的state=1和独占线程=线程1，线程2的waitStatus=SINGAL，线程2和线程3加入FIFO的等待队列中）

  ![企业微信截图_c9bcfb3f-eecf-41d4-ba84-7fa054ef4f6c.png](http://ww1.sinaimg.cn/large/87a42753ly1gg73abcpl7j21zw0okk22.jpg)

* 线程1释放锁，线程2获取锁

  线程1释放锁（state=0，独占线程=null），释放后会唤醒head节点的后置节点也就是线程2

  

  ![企业微信截图_2ec8bffa-5f93-460a-8919-f361da5def71.png](http://ww1.sinaimg.cn/large/87a42753ly1gg73hjbvgjj21za13uwu4.jpg)

  ![企业微信截图_17b74c9c-b6ae-4801-83b4-1ab53de998b0.png](http://ww1.sinaimg.cn/large/87a42753ly1gg73jvfsh1j221o0q0n8n.jpg)

* 公平锁

  **非公平锁**是`ReentrantLock`的默认实现

  * 执行lock()的时候，先尝试用CAS获取一次锁，若获取不到才会进入一个队列等待锁释放

  ![企业微信截图_6d5fbf4c-6778-4c39-8edf-dc801e7383db.png](http://ww1.sinaimg.cn/large/87a42753ly1gg73mjs93cj21wi13sqi9.jpg)

  当**线程二**释放锁的时候，唤醒被挂起的**线程三**，**线程三**执行`tryAcquire()`方法使用`CAS`操作来尝试修改`state`值，如果此时又来了一个**线程四**也来执行加锁操作，同样会执行`tryAcquire()`方法。

  这种情况就会出现竞争，**线程四**如果获取锁成功，**线程三**仍然需要待在等待队列中被挂起。这就是所谓的**非公平锁**，**线程三**辛辛苦苦排队等到自己获取锁，却眼巴巴的看到**线程四**插队获取到了锁。

  #### 公平锁实现

  公平锁在加锁的时候，会先判断`AQS`等待队列中是存在节点并且当前线程不是锁的持有者，如果符合会直接入队等待

  #### 异同

  **非公平锁**和**公平锁**的区别：**非公平锁**性能高于**公平锁**性能。**非公平锁**可以减少`CPU`唤醒线程的开销，整体的吞吐效率会高点，`CPU`也不必取唤醒所有线程，会减少唤起线程的数量

  **非公平锁**性能虽然优于**公平锁**，但是会存在导致**线程饥饿**的情况。在最坏的情况下，可能存在某个线程**一直获取不到锁**。不过相比性能而言，饥饿问题可以暂时忽略，这可能就是`ReentrantLock`默认创建非公平锁的原因之一了。

* Condition中await和signal

  `Condition`是在`java 1.5`中才出现的，它用来替代传统的`Object`的`wait()`、`notify()`实现线程间的协作，相比使用`Object`的`wait()`、`notify()`，使用`Condition`中的`await()`、`signal()`这种方式实现线程间协作更加安全和高效。

  ![企业微信截图_eee70064-48b7-4120-948f-dba875100738.png](http://ww1.sinaimg.cn/large/87a42753ly1gg73rtj2uij220s11cdwf.jpg)

### ReentrantLock之Condition

* await的时候，当前线程是有锁的，加入到condition的等待队列中
* signal的时候，当前线程是有锁的，从等待队列加到aqs的阻塞队列中

至于具体的锁竞争和condition无关，condition只是在操作锁的挂起和唤醒，不操作锁的获取

### 线程调度LockSupport（先unpark再park）

挂起和唤醒是线程调度中和锁的实现最密切的操作，juc 中通过一个 LockSupport 来抽象这两种操作，它是创建锁和其它同步类的基础。

- LockSupport和每个使用它的线程都与一个许可(permit)关联。permit相当于1，0的开关，默认是0，调用一次unpark就加1变成1，调用一次park会消费permit, 也就是将1变成0，同时park立即返回。再次调用park会变成block（因为permit为0了，会阻塞在这里，直到permit变为1）, 这时调用unpark会把permit置为1。每个线程都有一个相关的permit, permit最多只有一个，重复调用unpark也不会积累。
- LockSupport 内部使用 Unsafe 类实现

和Object的wait和notify的异同

（1）wait和notify都是Object中的方法,在调用这两个方法前必须先获得锁对象，但是park不需要获取某个对象的锁就可以锁住线程。

（2）notify只能随机选择一个线程唤醒，无法唤醒指定的线程，unpark却可以唤醒一个指定的线程。

（3）notify先唤醒后await会死锁，LockSupport不会，因为是许可证

### synchorized和ReentrantLock异同

* **两者都是可重入锁**

  两者都是可重入锁。“可重入锁”概念是：自己可以再次获取自己的内部锁。比如一个线程获得了某个对象的锁，此时这个对象锁还没有释放，当其再次想要获取这个对象的锁的时候还是可以获取的，如果不可锁重入的话，就会造成死锁。同一个线程每次获取锁，锁的计数器都自增1，所以要等到锁的计数器下降为0时才能释放锁。

* **synchronized 依赖于 JVM 而 ReentrantLock 依赖于 API**

  synchronized 是依赖于 JVM 实现的，前面我们也讲到了 虚拟机团队在 JDK1.6 为 synchronized 关键字进行了很多优化，但是这些优化都是在虚拟机层面实现的，并没有直接暴露给我们。ReentrantLock 是 JDK 层面实现的（也就是 API 层面，需要 lock() 和 unlock() 方法配合 try/finally 语句块来完成），所以我们可以通过查看它的源代码，来看它是如何实现的。

* **ReentrantLock 比 synchronized 增加了一些高级功能**

  相比synchronized，ReentrantLock增加了一些高级功能。主要来说主要有三点：**①等待可中断；②可实现公平锁；③可实现选择性通知（锁可以绑定多个条件）**

  - **ReentrantLock提供了一种能够中断等待锁的线程的机制**，通过lock.lockInterruptibly()来实现这个机制。也就是说正在等待的线程可以选择放弃等待，改为处理其他事情。
  - **ReentrantLock可以指定是公平锁还是非公平锁。而synchronized只能是非公平锁。所谓的公平锁就是先等待的线程先获得锁。** ReentrantLock默认情况是非公平的，可以通过 ReentrantLock类的`ReentrantLock(boolean fair)`构造方法来制定是否是公平的。
  - synchronized关键字与wait()和notify()/notifyAll()方法相结合可以实现等待/通知机制，ReentrantLock类当然也可以实现，但是需要借助于Condition接口与newCondition() 方法。Condition是JDK1.5之后才有的，它具有很好的灵活性，比如可以实现多路通知功能也就是在一个Lock对象中可以创建多个Condition实例（即对象监视器），**线程对象可以注册在指定的Condition中，从而可以有选择性的进行线程通知，在调度线程上更加灵活。 在使用notify()/notifyAll()方法进行通知时，被通知的线程是由 JVM 选择的，用ReentrantLock类结合Condition实例可以实现“选择性通知”** ，这个功能非常重要，而且是Condition接口默认提供的。而synchronized关键字就相当于整个Lock对象中只有一个Condition实例，所有的线程都注册在它一个身上。如果执行notifyAll()方法的话就会通知所有处于等待状态的线程这样会造成很大的效率问题，而Condition实例的signalAll()方法 只会唤醒注册在该Condition实例中的所有等待线程。

* synchronized会自动释放锁，而Lock必须手动释放锁。

* synchronized是不可中断的，Lock可以中断也可以不中断。

* 通过Lock可以知道线程有没有拿到锁，而synchronized不能。

* synchronized能锁住方法和代码块，而Lock只能锁住代码块。

* ReenTrantLock提供了一个Condition（条件）类，用来实现分组唤醒需要唤醒的线程们，而不是像synchronized要么随机唤醒一个线程要么唤醒全部线程。

* Lock可以使用读锁提高多线程读效率。

### AQS的node状态

- shared ：共享模式
- exclusive：独占模式
- cancelled=1：当前节点的线程是已取消的
- signal=-1：当前节点的线程是需要被唤醒的
- condition=-2：当前节点的线程正在等待某个条件
- propagate=-3：表示下一个共享模式的节点应该无条件的传播下去

### AQS的共享

- **Semaphore(信号量)-允许多个线程同时访问：** synchronized 和 ReentrantLock 都是一次只允许一个线程访问某个资源，Semaphore(信号量)可以指定多个线程同时访问某个资源。

- **CountDownLatch （倒计时器）：** CountDownLatch是一个同步工具类，用来协调多个线程之间的同步。这个工具通常用来控制线程等待，它可以让某一个线程等待直到倒计时结束，再开始执行。

  - 工作步骤

  ​          初始化时定义几个任务，即同步器中state的数量

  ​          等待线程执行await方法等待state变成0，等待线程会进入同步器的等待队列

  ​          任务线程执行countDown方法之后，state值减1，知道减到0，唤醒等待队列中所有的等待线程

  - 同步器
    实现了tryAcquireShared方法，判断state！=0的时候把等待线程加入到等待队列并阻塞等待线程，state=0的时候这个latch就不能够再向等待队列添加等待线程；另外实现了tryReleaseShared，判断当前任务是否是最后一个任务，当state减到0的时候就是最后一个任务，然后会以传播唤醒的方式唤醒等待队列中的所有等待线程。

- **CyclicBarrier(循环栅栏)：** CyclicBarrier 和 CountDownLatch 非常类似，它也可以实现线程间的技术等待，但是它的功能比 CountDownLatch 更加复杂和强大。主要应用场景和 CountDownLatch 类似。CyclicBarrier 的字面意思是可循环使用（Cyclic）的屏障（Barrier）。它要做的事情是，让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续干活。Cyc licBarrier默认的构造方法是 CyclicBarrier(int parties)，其参数表示屏障拦截的线程数量，每个线程调用await()方法告诉 CyclicBarrier 我已经到达了屏障，然后当前线程被阻塞。

### 读写锁

AQS只维护了一个state状态变量，ReentrantReadWriteLock利用其高 16 位表示读状态也就是获取该读锁的线程个数，低 16 位表示获取到写锁的线程的可重入次数。

