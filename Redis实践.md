# Redis实践

> RedisUtil是工具类，封装了Java对redis操作

## 1.计数

+ 限制接口访问量   **INCR**

  *场景：为了防止询价接口被恶意刷，需要限制用户每日询价次数*

  key：evaluate_limit_+用户唯一标识UID     例如：evaluate_limit_391128438125228

  

  >```
  >String value = RedisUtil.get(key);
  >Integer count = Strings.isEmpty(value)? 0:Integer.parseInt(value);
  >if(count>=20) {
  >	return Result.notOk(-1, "已达到今日询价次数上限")
  >}
  >....
  > 估价
  >....
  >RedisUtil.incr(key, 24*60*60);
  >```
  >
  >

+ 统计日活，七日活，月活  **HyperLogLog **

  key：activity_日期   例如：activity_20200924  value:用户唯一标识比如用户UID

  >Redisutil.pfAdd(key,UID );
  
  
  
  统计日活的话，只需要根据当日的日期拼接key：activity_+当日日期，然后调用HyperLogLog 的
  
  PFCOUNT即可；统计月活的话，使用PFMERGE命令合并该月每日的HyperLogLog 存储的数值即可。
  
  

## 2.缓存

+ **String**

  *场景：小程序AccessToken每两个小时需要获取一次，获取之后存到缓存中，同时设置缓存时间为expire_time。*

  ![image-20200924155253313](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200924155253313.png)

+ **List**

  *场景：批量生成太阳码，放入list构成的队列中，需要的时候从队列中获取，避免接口直接调用小程序接口带来的接口超时*

  *预生成太阳码并放入队列中*

  ![image-20200924162534396](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200924162534396.png)

  ![image-20200924162635579](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200924162635579.png)

  *获取太阳码*

  ![image-20200924162319751](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200924162319751.png)

+ **Hash **   

  *场景：存储对象*

  ![img](https://upload-images.jianshu.io/upload_images/4933701-bbc000b2ff755ab9.png?imageMogr2/auto-orient/strip|imageView2/2/w/424/format/webp)

  比起将对象转化为json字符串再存入缓存，直接用Hash存储对象的话，可以单独更改某个filed的值。`hset  key field value`

  `hset`是以哈希散列表的形式存储，超时时间只能设置在键`key`上，单个域`field`不能设置过期时间。

+ **Sorted set**

  *场景：需要记录用户的搜索记录并且把最近一个月的历史搜索记录展示给用户*（按照用户搜索时的时间倒序）

  *添加用户搜索记录*

  ![image-20200924165058027](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200924165058027.png)

  *注意score为当前时间时间戳+30天的毫秒数*

  获取用户搜索记录

  ![image-20200924165557697](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200924165557697.png)



	相比于集合Set，有序集合增加了score，引入了分数的概念，可以按照分数来进行排序。比如
	
	排行榜。在实际应用中，可以按照是否需要排序来选择使用集合Set还是有序集合Sorted set

## 3.消息队列  List

除了使用list作为队列缓存数据之外，List也可以作为消息队列。不过由于已经有比较流行的RocketMq

等消息中间件，所以在实际的应用中一般不使用Redis作为消息队列。

## 4.分布式锁

分布式锁的实现方式有多种，比如数据库，etcd，zookeeper等，比较热门的还有redis来实现分布式锁。感兴趣可以查阅先关分布式锁资料

