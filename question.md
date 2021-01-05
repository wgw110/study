1.volatile用于单例

>```java
>public class DoubleCheckedLocking { // 1
>	private static volatile Instance instance; // 2
>	public static Instance getInstance() { // 3
>		if (instance == null) { // 4:第一次检查
>			synchronized (DoubleCheckedLocking.class) { // 5:加锁
>				if (instance == null) // 6:第二次检查
>				instance = new Instance(); // 7:问题的根源出在这里
>			} // 8
>		} // 9
>		return instance; // 10
>	} // 11
>}
>```
>
>如果instance变量没有用volatile修饰：
>
>假设此时线程A执行到**到第4行的时候，代码读取到instance不为null时，instance引用的对象有可能还没有完成初始化。**
>
>为什么？
>
>主要的原因是重排序。创建了一个对象，这一行代码可以分解成3个操作：
>
>```
>memory = allocate();　　// 1：分配对象的内存空间
>ctorInstance(memory);　// 2：初始化对象
>instance = memory;　　// 3：设置instance指向刚分配的内存地址
>```
>
>2和3之间，可能会被重排序。例如：
>
>```
>memory = allocate();　　// 1：分配对象的内存空间
>instance = memory;　　// 3：设置instance指向刚分配的内存地址
>// 注意，此时对象还没有被初始化！
>ctorInstance(memory);　// 2：初始化对象
>```
>
>单线程下没有问题，但是多线程下可能出现线程A执行到3还没有执行对象初始化的时候，线程A由于CPU时间片到期了所以线程A让出CPU资源给线程B，但是线程B判断instance实例不为空于是放弃初始化直接去拿对象使用，这样线程B就会拿到一个未初始化的对象。如果把**instance声明为volatile**，那么由于volatile修饰的变量禁止重排序所以这个问题就解决了。

2.MESI缓存一致性

>![image-20201204100349908](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201204100349908.png)
>
>一个缓存区可以分为N个缓存行(Cache line)，缓存行是和内存进行数据交换的最小单位。每个缓存行包含三个部分，其中valid用于标识该数据的有效性。Tag用于指示数据对应的内存地址；block则用以存储数据；
>
>在MESI协议中，valid改成两位，不再只是有效和无效两种状态，而是有四个状态，分别为：
>
>M（Modified）：表示核心的数据被修改了，缓存数据属于有效状态，**但是数据只处于本核心对应的缓存，还没有将这个新数据写到内存中**。由于此时数据在各个核心缓存区只有唯一一份，不涉及缓存一致性问题；
>
>E (Exclusive)：表示**数据只存在本核心对应的缓存中，别的核心缓存没这个数据，缓存数据属于有效状态，并且该缓存中的最新数据已经写到内存中了**。同样由于此时数据在各个核心缓存区只有一份，也不涉及缓存一致性问题；
>
>S（Shared）：表示数据存于多个核心对应的缓存中，缓存数据属于有效状态，和内存一致。这种状态的值涉及缓存一致性问题；
>
>I（Invalid）：表示该核心对应的缓存数据无效。
>
>为了保证缓存一致性，每个核心要写新数据前，需要确保其他核心已经置同一变量数据的缓存行状态位为Invalid后，再把新数据写到自己的缓存行，并之后写到内存中。
>
>![image-20201204102636341](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201204102636341.png)
>
>MESI协议瓶颈：
>
>1.无效化指令通知其它的核心该变量所在cache line失效，在通知完之前，该核心不能做关于这个变量的操作；
>
>2.Kernel0在收到其它核心的确认响应之前，不能操作该变量，直到其它核心的确认响应完成(已将自己的cache line设置为valid)，Kernel0才能继续操作。
>
>优化以及引入的问题：
>
>Store Buffer(存储缓存)：针对无效化指令的加速，Store Buffer是一个特殊的硬件存储结构。通俗的来讲，核心可以先将变量写入Store Buffer，然后再处理其他事情。如果后面的操作需要用到这个变量，就可以从Store Buffer中读取变量的值，核心读数据的順序变成Store Buffer → 缓存 → 内存。这样在任何时候核心都不用卡住，做不了关于这个变量的操作了。
>
>Invalidate Queue(失效队列)：针对确认响应的加速，在缓存的基础上，引入Invalidate Queue这个结构。其他核心收到Kernel0的Invalidate的命令后，立即给Kernel0回Acknowledge，并把Invalidate这个操作，先记录到Invalidate Queue里，当其他操作结束时，再从Invalidate Queue中取命令，进行Invalidate操作。**所以当Kernel0收到确认响应时，其他核心对应的缓存行可能还没完全置为Invalid状态。**
>
>![image-20201204103804673](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201204103804673.png)
>
>Store Buffer与Invalidate Queue可以加速MESI协议整个过程的执行速度，但是由于Store Buffer中存储的值是还没有提交的，或者nvalidate Queue中的命令可能尚未执行，这就导致两个核心中读到的数据不一致出现缓存不一致的问题。
>
>内存屏障：
>
>针对Store Buffer：核心在后续变量的新值写入之前，把Store Buffer的所有值刷新到缓存，
>
>这种也称为内存屏障中的写屏障（Store Barrier）；
>
>针对Invalidate Queue：执行后需等待Invalidate Queue完全应用到缓存后，后续的读操作才能继续执行，这种也称为内存屏障中的读屏障（Load Barrier）。
>
>JVM的内存屏障有四种：
>
>LoadLoad屏障，StoreStore屏障，LoadStore屏障，StoreLoad屏障
>
>**LoadLoad屏障：**对于这样的语句Load1; LoadLoad; Load2，在Load2及后续读取操作要读取的数据被访问前，保证Load1要读取的数据被读取完毕。
>**StoreStore屏障：**对于这样的语句Store1; StoreStore; Store2，在Store2及后续写入操作执行前，保证Store1的写入操作对其它处理器可见。
>**LoadStore屏障：**对于这样的语句Load1; LoadStore; Store2，在Store2及后续写入操作被刷出前，保证Load1要读取的数据被读取完毕。
>**StoreLoad屏障：**对于这样的语句Store1; StoreLoad; Load2，在Load2及后续所有读取操作执行前，保证Store1的写入对所有处理器可见。它的开销是四种屏障中最大的。 
>
>针对volatile变量，JVM采用的内存屏障是：
>
>1. 针对volatile修饰变量的写操作：在写操作前插入StoreStore屏障，在写操作后插入StoreLoad屏障；
>2. 针对volatile修饰变量的读操作：在每个volatile读操作前插入LoadLoad屏障，在读操作后插入LoadStore屏障；
>
>通过这种方式，就可以保证被volatile修饰的变量具有线程间的可见性和禁止指令重排序的功能了。

#### Cache Line 伪共享

>核心：同一个 Cache Line可以存储多个变量
>
>Cache Line 伪共享问题，就是由多个 CPU 上的多个线程同时修改自己的变量引发的。这些变量表面上是不同的变量，但是实际上却存储在同一条 Cache Line 里。
>在这种情况下，由于 Cache 一致性协议，两个处理器都存储有相同的 Cache Line 拷贝的前提下，本地 CPU 变量的修改会导致本地 Cache Line 变成 `Modified` 状态，然后在其它共享此 Cache Line 的 CPU 上，
>引发 Cache Line 的 Invaidate 操作，导致 Cache Line 变为 `Invalidate` 状态，从而使 Cache Line 再次被访问时，发生本地 Cache Miss，从而伤害到应用的性能。
>在此场景下，多个线程在不同的 CPU 上高频反复访问这种 Cache Line 伪共享的变量，则会因 Cache 颠簸引发严重的性能问题。

### Java 锁升级

>![image-20201207105839630](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201207105839630.png)
>
>无锁(01)->偏向锁(01)->轻量级锁(00)->重量级锁(10)
>
>什么时候升级为轻量级锁：
>
>线程A已经拥有偏向锁，线程B进来请求获取同步资源，此时锁会升级为轻量级锁；
>
>什么时候升级为重量级锁：
>
>当自旋超过一定的次数，或者一个线程在持有锁，一个在自旋，又有第三个来访时，轻量级锁升级为重量级锁。
>
>无锁使用的CAS与轻量级锁的CAS是不一样的，无锁的CAS操作是指使用CAS对同步资源访问，最终只有一个线程可以访问成功，不涉及到对对象头的操作；而偏向锁的CAS是指将对象的Mark Word更新为指向Lock Record的指针，这里涉及到对对象头的操作(包括设置锁状态标志位)
>
>轻量级锁加锁：
>
>1.在代码进入同步块的时候，如果此同步对象没有被锁定（锁标志位为“01”状态），虚拟机首先将在当前线程的栈帧中建立一个名为锁记录（Lock Record）的空间，用于存储锁对象目前的Mark Word的拷贝
>
>![image-20201207105646568](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201207105646568.png)
>
>2.虚拟机将使用CAS操作尝试将对象的Mark Word更新为指向Lock Record的指针。如果这个更新动作成功，那么这个线程就拥有了该对象的锁，并且对象Mark Word的锁标志位（Mark Word的最后两个Bits）将转变为“00”
>
>![image-20201207105909915](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201207105909915.png)
>
>锁消除与锁粗化
>
>锁消除是指虚拟机即时编译器在运行时，对一些代码上要求同步，但是被检测到不可能存在共享数据竞争的锁进行消除。
>
>通常情况下，为了保证多线程间的有效并发，会要求每个线程持有锁的时间尽可能短，但是大某些情况下，一个程序对同一个锁不间断、高频地请求、同步与释放，会消耗掉一定的系统资源，因为锁的讲求、同步与释放本身会带来性能损耗，这样高频的锁请求就反而不利于系统性能的优化了，虽然单次同步操作的时间可能很短。**锁粗化就是告诉我们任何事情都有个度，有些情况下我们反而希望把很多次锁的请求合并成一个请求，以降低短时间内大量锁请求、同步、释放带来的性能损耗。**
>
>

### 本地缓存Caffeine

Caffeine提供了灵活的结构来创建缓存，并且有以下特性：

- 自动加载条目到缓存中，可选异步方式
- 可以基于大小剔除
- 可以设置过期时间，时间可以从上次访问或上次写入开始计算
- 异步刷新
- keys自动包装在弱引用中
- values自动包装在弱引用或软引用中
- 条目剔除通知(监听器)
- 缓存访问统计

>API和Guava Cache一致:
>
>```java
>Cache<String, String> cache = Caffeine.newBuilder()
>    .expireAfterWrite(1, TimeUnit.SECONDS)
>    .expireAfterAccess(1, TimeUnit.SECONDS)
>    .maxmumSize(100)
>    .build;
>```
>
>Caffeine缓存淘汰策略使用了更加高效的 **W-TinyLFU** 算法。该算法结合了LRU和LFU 算法。
>
>LRU算法(最近最少使用)：偶发性的、周期性的批量操作会导致LRU命中率急剧下降
>
>LFU（最近使用频率最低）：一是需要额外的存储空间记录访问次数，数据量大的时候对于存储的消耗比较大，另外对于某一个时间段的热点数据，可能这种数据在某一段时间访问频率高之后就不再或者访问频率下降了，但是由于记录的访问次数比较多导致需要很久才会被淘汰。
>
>在W-TinyLFU中使用Count-Min Sketch记录我们的访问频率，而这个也是布隆过滤器的一种变种。
>
>![img](https://mmbiz.qpic.cn/mmbiz_png/WLIGprPy3z7MHYOicCbZAAH082lsg15ODyiazAiajnb7c4hqNoma8tsSJoC6dTQOMLdialH2vjLIwUNK5DohUOIjaw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)
>
>要记录一个值，那我们需要通过多种Hash算法对其进行处理hash，然后在对应的hash算法的记录中+1，取记录中最小的作为该key的访问次数，上图就是取6作为该key的访问频率。
>
>在Caffeine的实现中，会先创建一个Long类型的数组，数组的大小为 2，数组的大小为数据的个数，如果你的缓存大小是100，他会生成一个long数组大小是和100最接近的2的幂的数，也就是128。另外，Caffeine将64位的Long类型划分为4段，每段16位，用于存储4种hash算法对应的数据访问频率计数。
>
>![image-20201209101001809](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201209101001809.png)
>
>RingBuffer队列：caffeine使用caffeine队列异步处理key过期淘汰，提升读写性能。
>
>#### 数据淘汰策略
>
><img src="https://mmbiz.qpic.cn/mmbiz_png/WLIGprPy3z7MHYOicCbZAAH082lsg15OD4BWqnpPzDibSoFpz9icPJRBUyic14DI6047mteXaJx5hQ43ia3VAkK89Cw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1" alt="img" style="zoom:50%;" />
>
>1. 所有的新数据都会进入Eden。
>2. Eden满了，淘汰进入Probation。
>3. 如果在Probation中访问了其中某个数据，则这个数据升级为Protected。
>4. 如果Protected满了又会继续降级为Probation。
>
>Eden队列:在caffeine中规定只能为缓存容量的%1,如果size=100,那这个队列的有效大小就等于1。这个队列中记录的是新到的数据，防止突发流量由于之前没有访问频率，而导致被淘汰，
>
>这个区域是最难被淘汰的区域。
>
>Probation队列:叫做缓刑队列，在这个队列就代表你的数据相对比较冷，马上就要被淘汰了。这个有效大小为size减去eden减去protected。
>
>Protected队列:在这个队列中，暂时不会被淘汰，但是如果Probation队列没有数据了或者Protected数据满了，也将会被面临淘汰的尴尬局面。当然想要变成这个队列，需要把Probation访问一次之后，就会提升为Protected队列。这个有效大小为(size减去eden) X 80%
>
>发生数据淘汰的时候，会从Probation中进行淘汰。会把这个队列中的数据队头称为受害者，这个队头肯定是最早进入的，按照LRU队列的算法的话那他其实他就应该被淘汰，但是在这里只能叫他受害者，这个队列是缓刑队列，代表马上要给他行刑了。这里会取出队尾叫候选者，也叫攻击者。这里受害者会和攻击者皇城PK决出我们应该被淘汰的。
>
>- 如果攻击者大于受害者，那么受害者就直接被淘汰。
>- 如果攻击者<=5，那么直接淘汰攻击者。
>- 其他情况，随机淘汰。

### GC

>MinorGC日志分析
>
>![image-20201211114718477](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201211114718477.png)
>
>Full/Major GC
>
>>2016-08-23T11:23:07.321-0200: 64.425: [**GC (CMS Initial Mark**) [1 CMS-initial-mark: 10812086K(11901376K)] 10887844K(12514816K), 0.0001997 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
>>2016-08-23T11:23:07.321-0200: 64.425: [**CMS-concurrent-mark-start**]
>>2016-08-23T11:23:07.357-0200: 64.460: [**CMS-concurrent-mark**: 0.035/0.035 secs] [Times: user=0.07 sys=0.00, real=0.03 secs]
>>2016-08-23T11:23:07.357-0200: 64.460: [**CMS-concurrent-preclean-start**]
>>2016-08-23T11:23:07.373-0200: 64.476: [**CMS-concurrent-preclean**: 0.016/0.016 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
>>2016-08-23T11:23:07.373-0200: 64.476: [**CMS-concurrent-abortable-preclean-start**]
>>2016-08-23T11:23:08.446-0200: 65.550: [**CMS-concurrent-abortable-preclean**: 0.167/1.074 secs] [Times: user=0.20 sys=0.00, real=1.07 secs]
>>2016-08-23T11:23:08.447-0200: 65.550: [GC (**CMS Final Remark**)
>>[YG occupancy: 387920 K (613440 K)]65.550: [Rescan (parallel) , 0.0085125 secs]65.559: 
>>[weak refs processing, 0.0000243 secs]65.559: [class unloading, 0.0013120 secs]65.560: 
>>[scrub symbol table, 0.0008345 secs]65.561: [scrub string table, 0.0001759 secs][1 CMS-remark: 10812086K(11901376K)] 11200006K(12514816K), 0.0110730 secs] 
>>[Times: user=0.06 sys=0.00, real=0.01 secs]
>>2016-08-23T11:23:08.458-0200: 65.561: [**CMS-concurrent-sweep-start**]
>>2016-08-23T11:23:08.485-0200: 65.588: [**CMS-concurrent-sweep**: 0.027/0.027 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
>>2016-08-23T11:23:08.485-0200: 65.589: [**CMS-concurrent-reset-start**]
>>2016-08-23T11:23:08.497-0200: 65.601: [C**MS-concurrent-reset**: 0.012/0.012 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
>
>
>
>GC七个阶段(https://www.codercto.com/a/45937.html)
>
>#### Phase 1: Initial Mark（初始化标记）
>
>主要标记过程
>
>- 从GC Roots遍历可直达的老年代对象；
>- 遍历被新生代存活对象所引用的老年代对象。
>
>![Java之CMS GC的7个阶段](https://img1.3s78.com/codercto/cb22385099c2d5861cc6824aeb503770)
>
>#### Phase 2: Concurrent Mark（并发标记）
>
>在该阶段， GC线程和应用线程将并发执行 。也就是说，在第一个阶段（Initial Mark）被暂停的应用线程将恢复运行。
>
>并发标记阶段的主要工作是， 通过遍历第一个阶段（Initial Mark）标记出来的存活对象，继续递归遍历老年代，并标记可直接或间接到达的所有老年代存活对象 。
>
>![Java之CMS GC的7个阶段](https://img1.3s78.com/codercto/4a3eba4c8da676819a94f0361a1ee27d)
>
>（Current obj：该对象的引用关系发生变化，对下一个对象的引用被删除。）
>
>在并发标记的阶段，用户线程与GC线程并发执行，所以可能产生新的对象或者对象的关系发生变化，例如：
>
>- 新生代的对象晋升到老年代；
>- 直接在老年代分配对象；
>- 老年代对象的引用关系发生变更；
>
>对于这些对象，需要重新标记以防止被遗漏。 为了提高重新标记的效率，本阶段会把这些发生变化的对象所在的Card(老年代划分为多个Card)标识为Dirty ，这样后续就只需要扫描这些Dirty Card的对象，从而避免扫描整个老年代。
>
>#### Phase 3: Concurrent Preclean（并发预清理）
>
>在并发预清洗阶段， 将会重新扫描前一个阶段标记的Dirty对象，并标记被Dirty对象直接或间接引用的对象，然后清除Card标识 。
>
>标记被Dirty对象直接或间接引用的对象：
>
>![Java之CMS GC的7个阶段](https://img1.3s78.com/codercto/41f34c05d7779ad16a9ddfa54388510d)
>
>清除Dirty对象的Card标识：
>
>![Java之CMS GC的7个阶段](https://img1.3s78.com/codercto/f0a773bd5326a27b59ae774bc76a3fc1)
>
>#### Phase 4: Concurrent Abortable Preclean（可中止的并发预清理）
>
>该阶段发生的前提是，新生代Eden区的内存使用量大于参数`CMSScheduleRemarkEdenSizeThreshold` 默认是2M
>
>**为什么需要这个阶段，存在的价值是什么？**
>因为CMS GC的终极目标是降低垃圾回收时的暂停时间，**所以在该阶段要尽最大的努力去处理那些在并发阶段被应用线程更新的老年代对象，这样在暂停的重新标记阶段就可以少处理一些，暂停时间也会相应的降低。**
>
>
>
>本阶段尽可能承担更多的并发预处理工作，从而 减轻在Final Remark阶段的stop-the-world 。
>
>在该阶段，主要循环的做两件事：
>
>- 处理 From 和 To 区的对象，标记可达的老年代对象；
>- 和上一个阶段一样，扫描处理Dirty Card中的对象。
>
>具体执行多久，取决于许多因素，满足其中一个条件将会中止运行：
>
>- 执行循环次数达到了阈值(CMSMaxAbortablePrecleanLoops)；
>- 执行时间达到了阈值(CMSMaxAbortablePrecleanTime)；
>- 新生代Eden区的内存使用率达到了阈值(CMSScheduleRemarkEdenPenetration)。
>
>如果在循环退出之前，发生了一次YGC，对于后面的Final Remark阶段来说，大大减轻了扫描年轻代的负担，但是发生YGC并非人为控制，所以只能祈祷这5s内可以来一次YGC。
>
>#### Phase 5: Final Remark（重新标记）
>
>预清理阶段也是并发执行的，并不一定是所有存活对象都会被标记，因为在并发标记的过程中对象及其引用关系还在不断变化中。
>
>因此， 需要有一个stop-the-world的阶段来完成最后的标记工作 ，这就是重新标记阶段（CMS标记阶段的最后一个阶段）。 主要目的是重新扫描之前并发处理阶段的所有残留更新对象 。
>
>- 遍历新生代对象，重新标记；（新生代会被分块，多线程扫描）
>- 根据GC Roots，重新标记；
>- 遍历老年代的Dirty Card，重新标记。这里的Dirty Card，大部分已经在Preclean阶段被处理过了。
>
>通常CMS尽量运行Final Remark阶段在年轻代是足够干净的时候，目的是消除紧接着的连续的几个STW阶段。
>
>CMS算法中提供了一个参数：`CMSScavengeBeforeRemark`，默认并没有开启，如果开启该参数，在执行Final Remark之前，会强制触发一次YGC，可以减少新生代对象的遍历时间，回收的也更彻底一点。
>
>#### Phase 6: Concurrent Sweep（并发清理）
>
>并发清理阶段，主要工作是 清理所有未被标记的死亡对象，回收被占用的空间 。
>
>![Java之CMS GC的7个阶段](https://img1.3s78.com/codercto/82d0795830038240b77be1de16b7d7b4)
>
>#### Phase 7: Concurrent Reset（并发重置）
>
>并发重置阶段，将 清理并恢复在CMS GC过程中的各种状态，重新初始化CMS相关数据结构 ，为下一个垃圾收集周期做好准备。
>
>#### 跨代引用：
>
>年轻代对象引用老年代对象或者老年代对象引用年轻代对象
>
>如果 young gc 线程只遍历年轻代内的对象引用，那么老年代到年轻代的跨代引用就会被忽略，被老年代存活对象跨代引用的年轻代对象会被回收，这样就破坏了应用程序的运行。
>
>![image-20201211164959863](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201211164959863.png)
>
>#### Card Marking
>
>在 jvm 中再次使用分治法，将老年代划分成多个 card（和 linux 内存 page 类似），只要 card 内对象引用被应用线程修改，就把 card 标记为 dirty。然后 young gc 时会扫描老年代中 dirty card 对应的内存区域，记录其中的跨代引用，这种方式被称为**Card Marking**。
>
>####  连续 STW
>
>如果 final-remark 阶段开始时刚好进行了 young gc（比如 ParNew）,应用程序刚因为 young gc 暂停，然后又会因为 final-remark 暂停，造成**连续的长暂停**，这就是连续的STW。除此之外，因为 young gc 线程修改了存活对象的引用地址，会产生很多需要重新扫描的对象，增加了 final-remark 的工作量。 所以 concurrent-abortable-preclean 除了 clean card 的作用，还有**调度 final-remark 开始时机**的作用。cms 回收器认为，final-remark 最理想的执行时机就是年轻代占用在 50%时(此时发生young gc的可能性比较小，不会造成连续的STW)。
>
>-XX:CMSScheduleRemarkEdenPenetration=50   表示当 eden 区内存占用到达 50%时，中断 abortable-preclean，开始执行 final-remark
>
>CMS中minor gc和major gc是顺序发生的吗？
>答：不是的，可以交叉发生，即在并发周期执行过程中，是可以发生Minor gc的，这个找个gc日志就可以观察到。
>
>CMS的并发收集周期会扫描哪些对象？会回收哪些对象？
>答：CMS的并发周期只会回收老年代的对象，但是在标记老年代的存活对象时，可能有些对象会被年轻代的对象引用，因此需要扫描整个堆的对象。
>
>

### JAVA 反射

>Class类：
>
>在java里，Class是一个实实在在的类，在包 java.lang 下，有这样一个Class.java文件，它跟我们自己定义的类一样，是一个实实在在的类，Class对象就是这个Class类的实例了。在Java里，所有的类的根源都是Object类，而Class也不例外，它是继承自Object的一个特殊的类，它内部可以记录类的成员、接口等信息，也就是在Java里，Class是一个用来表示类的类。
>
>Java运行在JVM之上，我们编写的类代码，在经过编译器编译之后，会为每个类生成对应的.class文件，这个就是JVM可以加载执行的字节码。运行时期间，当我们需要实例化任何一个类时，JVM会首先尝试看看在内存中是否有这个类，如果有，那么会直接创建类实例；如果没有，那么就会根据类名去加载这个类，当加载一个类，或者当加载器(class loader)的defineClass()被JVM调用，便会为这个类产生一个Class对象（一个Class类的实例），用来表达这个类，该类的所有实例都共同拥有着这个Class对象，而且是唯一的。
>
>**Class对象在java里被用来对类的情况进行表述的一个实例，也就是是类的实际表征，可以理解为是对规则的图表化，这样JVM才能直观的看懂，可以看做是一个模版；而类的实例化对象，就是通过模版，开辟出的一块内存进行实际的使用。**
>
>```java 
>Class mClassForName = Class.forName("com.dxjia.sample.Name");
>```
>
>我们使用Class.forName为com.dxjia.sample包下面的Name类创建一个Class对象，这个Class对象的引用就是mClassForName，针对每一个类，都只会创建**唯一的一个**Class对象，通过这个Class对象就可以获取对应的类中的变量，方法，并且可以生成一个该类的实例对象并通过实例对象操作方法等。这个名为mClassForName的Class对象就是用来描述Name类的结构信息的，JVM据此可以来为Name类生成实例对象并为实例变量赋初值。
>
>通过反射调用类的方法
>
>```java
>Class actionClass=Class.forName(“MyClass”);
>Object action=actionClass.newInstance();
>Method method = actionClass.getMethod(“myMethod”,null);
>method.invoke(action,null);
>```
>
>第一行：生成MyClass类的Class对象actionClass；
>
>第二行：根据actionClass的newInstance生成类的实例对象
>
>第三行：根据actionClass对象调用其getMethod方法获取MyClass中的名为myMethod的Method
>
>第四行 ：调用method.invoke()方法实现调用MyClass对象action的myMethod方法
>
>![image-20201211192054171](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201211192054171.png)
>
>![image-20201211192429502](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201211192429502.png)
>
>实际的MethodAccessor实现有两个版本，一个是Java实现的，另一个是native code实现的。Java实现的版本在初始化时需要较多时间，但长久来说性能较好；native版本正好相反，启动时相对较快，但运行时间长了之后速度就比不过Java版了。这是HotSpot的优化方式带来的性能特性，同时也是许多虚拟机的共同点：**跨越native边界会对优化有阻碍作用，它就像个黑箱一样让虚拟机难以分析也将其内联，于是运行时间长了之后反而是托管版本的代码更快些。**
>为了权衡两个版本的性能，Sun的JDK使用了“inflation”的技巧：让Java方法在被反射调用时，开头若干次使用native版，等反射调用次数超过阈值时则生成一个专用的MethodAccessor实现类，生成其中的invoke()方法的字节码，以后对该Java方法的反射调用就会使用Java版。

#### JDK 动态代理

>![image-20201212104933449](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201212104933449.png)
>
>![image-20201212105303011](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201212105303011.png)
>
>![image-20201212105834883](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201212105834883.png)
>
>![image-20201212110445099](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201212110445099.png)
>
>整个过程：先从缓存中查询是否有代理类，没有的话，需要先创建代理类，创建代理类的时候需要先检测接口属性，代理类与目标类是否属于同一接口，然后根据规则生成包名与类名，最后加载字节码获取代理对象的Class对象。获取到代理对象之后，需要对代理对象修饰符进行处理，使得代理类可以被访问，最后通过反射机制生成代理类的实例对象。
>
>![image-20201212111717963](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201212111717963.png)
>
>```java
>public class TargetInvoker implements InvocationHandler {
>    // 代理中持有的目标类
>    private Object target;
>
>    public TargetInvoker(Object target) {
>        this.target = target;
>    }
>
>    @Override
>    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
>        System.out.println("jdk 代理执行前");
>        Object result = method.invoke(target, args);
>        System.out.println("jdk 代理执行后");
>        return result;
>    }
>}
>```
>
>在`InvocationHandler#invoker`中必须调用目标类被代理的方法，否则无法做到代理的实现。

#### CGLIB动态代理

>CGLIB 动态代理的实现机制是生成目标类的子类，通过调用父类（目标类）的方法实现，在调用父类方法时在代理中进行增强。
>
>![image-20201212112239820](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201212112239820.png)
>
>![image-20201212112343491](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201212112343491.png)
>
>![image-20201212112820250](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201212112820250.png)
>
>CGLIB 的实现原理是通过设置被代理的类信息到 Enhancer 中，然后利用配置信息在`Enhancer#create`生成代理类对象。生成类是使用 ASM 进行生成
>
>区别：
>
>JDK 动态代理是通过实现目标类的接口，然后将目标类在构造动态代理时作为参数传入，使代理对象持有目标对象，再通过代理对象的 InvocationHandler 实现动态代理的操作。
>CGLIB 动态代理是通过配置目标类信息，然后利用 ASM 字节码框架进行生成目标类的子类。当调用代理方法时，通过拦截方法的方式实现代理的操作。
>总的来说，JDK 动态代理利用接口实现代理，CGLIB 动态代理利用继承的方式实现代理。

#### AQS（AbstractQueuedSynchronizer）抽象同步队列

>![image-20201214200847776](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214200847776.png)
>
>![image-20201214200858626](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214200858626.png)
>
>AQS是一个FIFO的双向队列，队列元素类型为Node，Node中的thread变量用来存放进入AQS队列的线程，SHARED与EXCLUSIVE标记分别标识线程是因为获取共享，独占资源被阻塞放入队列之中，waitStatus表示线程的状态，分别有CANCELLED（线程被取消）、SINGAL(线程等待被唤醒)、CONDITION（线程在条件队列等待）、PROPAGATE(线程释放共享资源时需要通知其它节点)；pre与next分别指向当前节点的前驱以及后继节点；
>
>![image-20201214201850176](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214201850176.png)
>
>AQS中的竞态资源就是这个state变量，如果某个线程可以操作这个state变量，说明该线程获取到了锁资源。
>
>通过控制一个`int`类型的`state`变量来表示同步状态，使用一个内置的`FIFO`（先进先出）队列来构建工作队列操作。通过独占方式获取资源是与具体的线程绑定的，假如某个线程获取到了锁资源，那么会标记是该线程获取到了锁资源，假如该锁是可重入的，那么之后该线程再次获取锁资源时，只是将state变量值加1，另一个线程尝试获取锁失败则会创建一个Node节点放入同步队列；
>
>对于共享资源，state变量不是与某个线程具体绑定的，只有线程对CAS就行操作的时候可以成功，那么该线程就可以获取到锁资源。
>
>![image-20201214204118769](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214204118769.png)
>
>![image-20201214204206393](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214204206393.png)
>
>![image-20201214204239958](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214204239958.png)
>
>共享方式与独占方式类似，只是AQS中的Node节点类型为SHARED
>
>![image-20201214204521874](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214204521874.png)
>
>![image-20201214210807217](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214210807217.png)
>
>![image-20201214214356732](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214214356732.png)
>
>![image-20201214214408439](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201214214408439.png)
>
>
>
>同步器的设计采用**模板模式**，要实现一个同步组件得先继承`AbstractQueuedSynchronizer`类，通过调用同步器提供的方法和重写同步器的方法来实现。调用同步器中的方法就是调用前面提到的通过`state`变量值的操作来表示同步操作，`state`是被`volatile`修饰来保证线程可见性。
>
>

#### Thread

> Java线程状态
>
>![image-20201224112240307](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224112240307.png)
>
>![image-20201224112258841](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224112258841.png)
>
>注意：使用Java JUC提供的lock锁，线程争夺锁失败会导致线程进入waiting状态而不是Bolcked状态，因为JUC下面最终使用的是LockSupport来挂起线程的。
>
>![image-20201224114246442](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224114246442.png)
>
>![image-20201224140028122](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224140028122.png)
>
>内核线程：
>
><img src="https://static.oschina.net/uploads/space/2017/0821/101338_3qYo_1859679.png" alt="内核级线程实现" style="zoom:67%;" />
>
>内核线程（KLT）就是直接由操作系统内核支持的线程，这种线程由内核来完成线程切换。
>
>程序一般不会直接使用内核线程，而是去使用内核线程的一种高级接口—轻量级进程（LWP），轻量级进程就是我们所讲的线程，这种轻量级进程与内核线程之间1：1的对应关系。
>
>![image-20201224140316983](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224140316983.png)
>
>![image-20201224140410158](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224140410158.png)
>
>![image-20201224140559869](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224140559869.png)
>
>### 线程中断
>
>![image-20201224140838206](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224140838206.png)
>
>
>![image-20201224141438126](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224141438126.png)
>
>![image-20201224141620246](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224141620246.png)
>
>![image-20201224142203262](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20201224142203262.png)

