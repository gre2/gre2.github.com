---
layout: post
title: "lambda"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}

[toc]

### 函数接口的意义

|  函数式接口   | 函数描述符  | lamdba例子  | reference  |
|  ----  | ----  |----  | ----  |
| Predicate<T> | T->boolean  [test] | (List<String> list)->list.isEmpty() | https://blog.csdn.net/w605283073/article/details/89410918lamdba |
| Function<T,R> | T->R [apply] | (String s)->s.length() | https://www.pianshen.com/article/2042114445/ |
| Consumer<T> | T→void  [void] | (Apple a)->System.out.println(a.getWeight()) | https://blog.csdn.net/qq_28410283/article/details/80618456 |
| Supplier<T> | ()->T  [get] | ()→new Apple(10) | https://blog.csdn.net/qq_28410283/article/details/80625482 |

### lamdba表达式的格式

* (params) -> expression

  ```
  () -> System.out.println("Hello Lambda Expressions");
  ```

* (params) -> statement

  ```
  (int even, int odd) -> even + odd
  ```

* (params) -> { statements }     [ ()->{}替代匿名内部类 ]

  ```
  // Java 8之前：
  new Thread(new Runnable() {
  @Override
  public void run() {
  System.out.println("Before Java8, too much code for too little to do");
  }
  }).start();
  
  //Java 8方式：
  new Thread( () -> System.out.println("In Java8, Lambda expression rocks !!") ).start();
  ```

### sort演进

```
//代码传递
public class AppleComparator implements Comparator<Apple> { 
    public int compare(Apple a1, Apple a2){ 
    	return a1.getWeight().compareTo(a2.getWeight()); 
    }
} 
inventory.sort(new AppleComparator());

//匿名类
inventory.sort(new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2){        
    	return a1.getWeight().compareTo(a2.getWeight());    
    } 
});

//lamdba表达式
inventory.sort((Apple a1, Apple a2)-> a1.getWeight().compareTo(a2.getWeight()) ); 

//方法引用
//替代那些转发参数的lambda表达式的语法糖
inventory.sort(comparing(Apple::getWeight)); 

//复合比较器
inventory.sort(comparing(Apple::getWeight).reversed().thenComparing(Apple::getCountry)); 

//空和数字的比较
list.stream().sorted(Comparator.nullsLast(Integer::compareTo).thenComparing(Integer::compareTo)).forEach(System.out::println);
或者
list.sort(Comparator.nullsFirst(Integer::compareTo).thenComparing(Integer::compareTo));
list.forEach(System.out::println);

//自定义比较器
public static void main(String[] args) {
    List<Integer> list = Lists.newArrayList();
 	list.add(1);list.add(2);list.add(3);list.add(4);   
 	list.add(5);list.add(0);list.add(null);list.add(null);
 	
 	//i1 =2，i1 =1
 	list.sort((i1, i2) -> {
        if(i1 == null || i1 == 0) {
            return -1;
 	 } else if(i2 == null || i2 == 0) {
            return 1;
	 }
        return i2 - i1;
 	});
 	for (Integer i : list) {
        System.out.println(i);
 	}
}

//复合谓词（negate[非]，and，or）===  a.or(b).and(c)===(a||b) && c
Predicate<Apple> redAndHeavyAppleOrGreen =     redApple.and(a -> a.getWeight() > 150) .or(a -> "green".equals(a.getColor()));
```

### java8-Stream

filter，map，reduce(归约)，find，match，sort，limit，skip（扔掉前几个元素），flatMap(Arrays::stream)（把流扁平化为单个流）

查找：allMatch、anyMatch、noneMatch、findFirst和findAny

* flatMap

  ```
  //题目：给定列表[1, 2, 3]和列表[3, 4]，应 该返回[(1, 3), (1, 4), (2, 3), (2, 4), (3, 3), (3, 4)]
  
  思路：你可以使用两个map来迭代这两个列表，并生成数对。但这样会返回一个Stream- <Stream<Integer[]>>。你需要让生成的流扁平化，以得到一个Stream<Integer[]>。
  
  List<Integer> numbers1 = Arrays.asList(1, 2, 3);
  List<Integer> numbers2 = Arrays.asList(3, 4);
  
  List<int[]> pairs =numbers1.stream().flatMap(i -> numbers2.stream().map(j -> new int[]{i, j}) ).collect(toList()); 
  
  //list嵌套
  List flatList=listList.stream().flatMap(Collection::stream).collect(Collectors.toList());
  ```

* reduce

  ```
  reduce接受两个参数：一个初始值，这里是0；一个BinaryOperator<T>来将两个元素结合起来产生一个新值，这里我们用的是 lambda (a, b) -> a + b。 
  int sum = numbers.stream().reduce(0, Integer::sum);//和mapToInt 
  或者
  int sum=numbers.stream().mapToInt(Info::getNo).sum;
  int sum=numbers.stream.mapToInt(Integer::intValue).sum();
  
  
  //reduce还有一个重载的变体，不接受初始值，但是会返回一个Optional的对象
  Optional<Integer> sum = numbers.stream().reduce((a, b) -> (a + b));
  
  //算菜品个数
  int count = menu.stream().map(d -> 1) .reduce(0, (a, b) -> a + b); 
  或者long count = menu.stream().count();
  
  //给每笔订单增加12%的税费
  List costBeforeTax = Arrays.asList(100, 200, 300, 400, 500);
  double bill = costBeforeTax.stream().map((cost) -> cost + .12*cost).reduce((sum, cost) -> sum + cost).get();
  ```

* 最大，最小，平均值，总和===mapToInt

  ```
  IntStream、LongStream 和 DoubleStream 等流的类中，有个非常有用的方法叫做 summaryStatistics() 。可以返回 IntSummaryStatistics、LongSummaryStatistics 或者 DoubleSummaryStatistics，描述流中元素的各种摘要数据。
  
  //获取数字的个数、最小值、最大值、总和以及平均值
  List<Integer> primes = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);
  IntSummaryStatistics stats = primes.stream().mapToInt((x) -> x).summaryStatistics();
  System.out.println("Highest prime number in List : " + stats.getMax());
  System.out.println("Lowest prime number in List : " + stats.getMin());
  System.out.println("Sum of all prime numbers : " + stats.getSum());
  System.out.println("Average of all prime numbers : " + stats.getAverage());
  
  或者
  int sum = Arrays.stream(numbers).sum(); 
  Optional<Transaction> optional =list.stream().min(comparing(Transaction::getValue)); 
  ```

* allMatch

  ```
  boolean isHealthy = menu.stream()   .allMatch(d -> d.getCalories() < 1000); 
  ```

* 偶数流

  ```
  IntStream evenNumbers = IntStream.rangeClosed(1, 100) .filter(n -> n % 2 == 0); 
  ```

* toMap

  ```
  Map<String, String> collect = userList.stream().collect(Collectors.toMap(User::getId, User::getName));
  
  //2和3是等价的
  Map<String, User> collect1 = userList.stream().collect(Collectors.toMap(User::getId, t -> t));
  Map<String, User> collect2 = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
  
  //roleNames是list的string串，把他作为key，value是空的list
  Map<String, List<IamV2UserRoleDTO>> roleMap = roleNameStrings.stream().collect(toMap(Function.identity(), d -> new ArrayList()));
  
  //处理value,key不变
  Map<String, List<String>> collect1 = roles.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().stream().map(IamV2UserRoleDTO::getRealName).collect(toList())));
  
  //第三个参数是key重复的时候如何处理
  Map<String, String> collect = userList.stream().collect(Collectors.toMap(User::getId, User::getName, (n1, n2) -> n1 + n2));
  Map<Long, User> maps = userList.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));
  
  //第四个参数是我们希望返回的map类型，treemap可以根据key排序
  TreeMap<String, String> collect1 = userList.stream().collect(Collectors.toMap(User::getId, User::getName, (n1, n2) -> n1, TreeMap::new));
  ```

* 合并两个map===4种方式

  ```
  Map<String, List<IamV2UserRoleDTO>> collect = Stream.of(roleMap, userMap)
  .flatMap(x -> x.entrySet().stream())
  .collect(toMap(Map.Entry::getKey,Map.Entry::getValue, (value1, value2) -> {
                          value1.addAll(value2);
                          return value1;
                      }));
                      
  refence:https://blog.csdn.net/tangyaya8/article/details/91399650
  ```

* groupingby

  ```
  Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel = menu.stream().collect( groupingBy(Dish::getType, groupingBy(dish -> 
  { 
  	if (dish.getCalories() <= 400) return CaloricLevel.DIET; 
  	else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL; 
   	else return CaloricLevel.FAT; 
  } ) ) );
  
  Map<String, List<List<String>>> map2 = conditions.stream().collect(Collectors.groupingBy(Condition::getCondName, Collectors.mapping(Condition::getCondValue, Collectors.toList())));
  
  Map<Dish.Type, Long> typesCount = menu.stream().collect(groupingBy(Dish::getType, counting())); 
  
  Map<String, Apple> collect3 = list.stream().collect(Collectors.groupingBy(Apple::getName, Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingInt(Apple::getWeight)), Optional::get)));
  
  Map<String, Integer> collect4 = list.stream().collect(Collectors.groupingBy(Apple::getName, Collectors.summingInt(Apple::getWeight)));
  ```

* 遍历map

  ```
  Iterator<Map.Entry<Integer, Integer>> it = map.entrySet().iterator(); while (it.hasNext()) { Map.Entry<Integer, Integer> entry = it.next(); System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue()); }
  
  //或者
  map.forEach((key, value) -> { System.out.println(key + ":" + value); });
  ```

* joining

  ```
  //每次都创建一个String对象，优化下（joining内部是stringbuilder）
  String shortMenu = menu.stream().map(Dish::getName).collect(joining()); 
  String collect2 = list.stream().map(Apple::getName).collect(Collectors.joining(","));
  ```

* 文件生成一个流

  ```
  long uniqueWords = 0;
  
  try(Stream<String> lines = Files.lines(Paths.get("data.txt"), Charset.defaultCharset())){
  	uniqueWords = lines.flatMap(line -> Arrays.stream(line.split(" "))).distinct() .count();
  } catch(IOException e){ }
  ```

* map转list

  ```
  List<CandidateInfo> list=map.entrySet().stream().map(entry->entry.getValue()).flatMap(List::stream).collect(Collectors.toList());
  ```

### 高级特性

* Future

  ```
  ExecutorService executorService=Executors.newCachedThreadPool();
  Future<String> future = executorService.submit(new Callable<String>() {
      public String call() {
          return "";
   }
  });
  try {
      future.get(1, TimeUnit.MILLISECONDS);
  } catch (Exception e) {
      e.printStackTrace();
  }
  //不能表述多个future结果的依赖性
  ```

* CompletableFuture

  ```
  CompletableFuture<String> completableFuture=new CompletableFuture();
  new Thread(()->{completableFuture.complete("");}).start();
  外边用Future对象接，获取值的时候future.get();
  
  CompletableFuture.supplyAsync(()->{return "";});
  ```

### 代码写法优化

* 链路追踪traceId【龙湖core包里面有了】【请求在线程池分发，龙湖core没有支持，需要重新封装一下线程池，把traceId带进去】

  ```
  @Override
  public <T> Future<T> submit(Runnable task, T result) {
      return super.submit(ThreadMdcUtil.wrap(task, MDC.getCopyOfContextMap()), result);
  }
  public static <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
      return () -> {
          if (context == null) {
              MDC.clear();
          } else {
              MDC.setContextMap(context);
          }
          setTraceIdIfAbsent();
          try {
              return callable.call();
          } finally {
              MDC.clear();
          }
      };
  }
  ```

* 替换if，else【策略模板模式】===接口，抽象类，实现类

  ```
  public interface CandidateRegisterProcessor {
   
      /**
       * 责任链的检查顺序，小的在前面，default必须在最后
       */
      int a = 0;
      int b = 1;
      int c = Integer.MAX_VALUE;
   
      int getPriority();
   
      boolean match(Context context);
   
      Result register(Context context);
  }
   
  public abstract class CandidateAbstractRegisterProcessor implements CandidateRegisterProcessor {
      //可以做macth和register的公共逻辑
  }
   
   
  @Service
  public class CandidateMultipleRegisterProcessor extends CandidateAbstractRegisterProcessor {
   
      @Override
      public int getPriority() {
          return CandidateRegisterProcessor.REGISTER_PROCESS_PRIORITY_MULTIPLE;
      }
   
      @Override
      public boolean match(CandidateRegisterContext context) {
          return true;
      }
   
   
      @Override
      protected CandidateRegisterResult doRegister(CandidateRegisterContext context) {
          return null;
      }
   
   
  }
   
   
  public class CandidateRegisterHelper {
   
      private static void checkOrInitParam(CandidateRegisterContext context) {
          Preconditions.checkArgument(context.getOrder() != null, "order不能为空");
          Preconditions.checkArgument(context.getRequest() != null, "request不能为空");
      }
   
      private static void preHandleRegisterProcessors(List<CandidateRegisterProcessor> processors) {
          Safes.of(processors)
                  .sort(Comparator.comparingInt(CandidateRegisterProcessor::getPriority));
      }
   
      public static CandidateRegisterResult processRegister(CandidateRegisterContext context, List<CandidateRegisterProcessor> processors) {
          checkOrInitParam(context);
          preHandleRegisterProcessors(processors);
          return Safes.of(processors).stream()
                  .filter(processor -> processor.match(context))
                  .findFirst()
                  .orElseThrow(() -> new RuntimeException("[候补锁号]未找到匹配的Processor"))
                  .register(context);
      }
   
  }
   
   
  //业务调用
  @Component
  public class CandidateMatchOrderConsumer implements MessageListener {
      @Resource
      private List<CandidateRegisterProcessor> candidateRegisterProcessors;
   
      //mq接收数据，循环所有message===入参vo和接口列表
      CandidateRegisterResult result = CandidateRegisterHelper.processRegister(
          new CandidateRegisterContext(order, request),
          candidateRegisterProcessors);
  }
  ```

  ```
  @Override
      public boolean match(RocketMessageExt<Map<String, Object>> map) {
          // common deal
          return this.doMatch(map);
      }
   
   
      @Override
      public void deal(RocketMessageExt<Map<String, Object>> map) {
          // common deal
          this.doDeal(map);
      }
   
   
      protected abstract boolean doMatch(RocketMessageExt<Map<String, Object>> map);
   
      protected abstract void doDeal(RocketMessageExt<Map<String, Object>> map);
  ```

* guaua cache（业务获取 + 定期刷新）

  从LoadingCache查询的正规方式是使用`get(K)`方法。这个方法要么返回已经缓存的值，要么使用CacheLoader向缓存原子地加载新值（通过`load(String key)` 方法加载）。

  简单使用：https://www.cnblogs.com/fnlingnzb-learner/p/11022152.html

  ```
  package com.longfor.plm.todo.util;
   
  import com.google.common.base.Splitter;
  import com.google.common.cache.CacheBuilder;
  import com.google.common.cache.CacheLoader;
  import com.google.common.cache.LoadingCache;
  import org.apache.commons.collections.CollectionUtils;
  import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
   
  import javax.annotation.PostConstruct;
  import java.util.List;
  import java.util.Optional;
  import java.util.Set;
  import java.util.concurrent.ExecutionException;
  import java.util.concurrent.Executors;
  import java.util.concurrent.ScheduledExecutorService;
  import java.util.concurrent.TimeUnit;
   
  import static java.util.concurrent.TimeUnit.MILLISECONDS;
   
  public class LoadCacheUtils {
   
      private static final int EXPIRE = 24 * 60 * 60 * 1000;
   
      private static final String SPLITTER = "ABC123abc";
   
      private static final ScheduledExecutorService singleExecutor = Executors
              .newSingleThreadScheduledExecutor();
   
      @PostConstruct
      public void init() {
          System.out.println("2");
          this.refreshCache();
          singleExecutor.scheduleAtFixedRate(this::refreshCache, 60, 60, TimeUnit.SECONDS);
      }
   
      public void refreshCache() {
          try {
              System.out.println("3");
              // 1
              Set<String> doctorKeys = doctorCache.asMap().keySet();
              if (CollectionUtils.isNotEmpty(doctorKeys)) {
                  for (String key : doctorKeys) {
                      List<String> split = Splitter.on(SPLITTER).splitToList(key);
                      Optional<Doctor> doctor = Optional
                              .ofNullable(db.queryByCode(split.get(0), split.get(1)));
                      doctorCache.put(key, doctor);
                  }
              }
          } catch (Exception e) {
          }
      }
   
      private LoadingCache<String, Optional<Doctor>> doctorCache = CacheBuilder.newBuilder()
              .expireAfterWrite(EXPIRE, MILLISECONDS).maximumSize(20000)
              .build(new CacheLoader<String, Optional<Doctor>>() {
                  @Override
                  public Optional<Doctor> load(String hos$doctorCode) throws Exception {
                      System.out.println("4");
                      List<String> split = Splitter.on(SPLITTER).splitToList(hos$doctorCode);
                      return Optional.ofNullable(
                              db.queryByCode(split.get(0), split.get(1)));
                  }
              });
   
      //业务方法，获取数据
      public Optional<Doctor> loadDoctor(String hosCode, String doctorCode) {
          return new Loader<Optional<Doctor>>() {
              @Override
              public Optional<Doctor> load0(String key) {
                  try {
                      return doctorCache.get(hosCode + SPLITTER + doctorCode);
                  } catch (ExecutionException e) {
                      return Optional.ofNullable(db.queryByCode(hosCode, doctorCode));
                  }
              }
          }.load(hosCode);
      }
   
      private static abstract class Loader<T> {
   
          public T load(String key) {
              long start = System.currentTimeMillis();
              try {
                  return load0(key);
              } finally {
                  //监控
              }
          }
   
          abstract T load0(String key);
      }
   
      class Doctor{}
   
  }
  ```

  ```
  //业务调用
  Optional<Doctor> docOpt = LoadCacheUtils.loadDoctor(hosCode, doctorId);
  if (docOpt == null || !docOpt.isPresent() || docOpt.get() == null) {
       
  }
  ```

* 判断Preconditions

  ```
  Preconditions.checkArgument(StringUtils.isNotBlank(result.getCardNo()), "卡号不能为空！！");
  ```

* 限流==单机（单位时间内的请求数或者并发数），分布式

  ```
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface AccessLimit {
   
      /**
       * 默认每秒限制访问次数
       * @return
       */
      double defaultLimit() default 10;
  }
  ```

  ```
  package com.longfor.plm.todo.util;
   
  import com.google.common.base.Joiner;
  import com.google.common.collect.Lists;
  import com.google.common.collect.Maps;
  import com.google.common.util.concurrent.RateLimiter;
  import org.springframework.stereotype.Component;
  import org.springframework.web.method.HandlerMethod;
  import org.springframework.web.servlet.HandlerInterceptor;
  import org.springframework.web.servlet.ModelAndView;
   
  import javax.servlet.http.HttpServletRequest;
  import javax.servlet.http.HttpServletResponse;
  import java.lang.reflect.Method;
  import java.util.List;
  import java.util.Map;
  import java.util.Optional;
   
  @Component
  public class LimitInterceptor implements HandlerInterceptor {
      //配置中心数据
      private  List<Object> configs = Lists.newArrayList();
      //业务请求量数据，configs转换来的
      private Map<String, RateLimiter> limiterMap = Maps.newConcurrentMap();
      private static Joiner UNDERLINE_JOINER = Joiner.on("_").skipNulls();
   
      /** 获取令牌失败resCode */
      private static final int RATE_LIMIT_ERROR_CODE = 999;
      /** 获取令牌失败msg */
      private static final String RATE_LIMIT_ERROR_MSG = "请求过于频繁,请稍后再试";
   
      @Override
      public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object _handler) throws Exception {
          try {
              if (!(_handler instanceof HandlerMethod)) {
                  return true;
              }
              HandlerMethod handler = (HandlerMethod) _handler;
              Method method = handler.getMethod();
              boolean accessLimit = method.isAnnotationPresent(AccessLimit.class);
              if (!accessLimit) {
                  return true;
              }
              return doRateLimit(httpServletRequest, httpServletResponse, method);
          } catch (Throwable e) {
              return true;
          }
      }
   
      @Override
      public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
   
      }
   
      @Override
      public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
   
      }
   
   
      private boolean doRateLimit(HttpServletRequest request, HttpServletResponse response, Method method) {
          String uri = request.getRequestURI();
          String key = genKey(uri, "请求限制维度");
          RateLimiter rateLimiter = limiterMap.get(key);
          if (rateLimiter == null) {
              // build new RateLimiter
              double limit = calcLimit(uri, method);
              if (limit < 0) {
                  limit = 0.1;
              }
              rateLimiter = RateLimiter.create(limit);
              limiterMap.putIfAbsent(key, rateLimiter);
          }
   
          boolean acquire = rateLimiter.tryAcquire();
          if (!acquire) {
              WebUtil.responseJson(APIResponse.error(RATE_LIMIT_ERROR_CODE, RATE_LIMIT_ERROR_MSG), response);
              return false;
          }
          return true;
      }
   
      private String genKey(String uri, String hosCode) {
          return UNDERLINE_JOINER.join(uri, hosCode);
      }
   
   
      private double calcLimit(String uri, Method method) {
          Optional<Object> interfaceOpt = Safes.of(configs).stream().filter(config -> 1==1).findAny();
          AccessLimit accessLimit = method.getAnnotation(AccessLimit.class);
          double defaultLimit = accessLimit.defaultLimit();
          if (!interfaceOpt.isPresent()) {
              return defaultLimit;
          }
          Object hospitalOfflineInterface = interfaceOpt.get();
   
          return xx.get().getLimit();
      }
   
  }
  ```

  ```
  public static void responseJson(object value,ServletResponse response){
  	response.setContentType("application/json;charset=UTF-8");
  	try{
  		JsonUtils.instance.writeValue(response.getWriter,value);
  	}catch(Exception e){
  		throw UnsafeUtils.throwException(e);
  	}
  }
  ```

* 降级

  - 业务返回数据降级
  - hystrix降级
  - nginx降级（兜底数据）

* adaptor抽象赋值代码块

* Safes集合封装类

  ```
  import com.google.common.base.Preconditions;
  import com.google.common.collect.Lists;
  import org.apache.commons.lang3.StringUtils;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
   
  import java.math.BigDecimal;
  import java.util.*;
  import java.util.function.Consumer;
   
  /**
   * 避免一些异常情况的工具类
   *
   */
  public class Safes {
   
      final private static Logger logger = LoggerFactory.getLogger(Safes.class);
   
      public static <K, V> Map<K, V> of(Map<K, V> source) {
          return java.util.Optional.ofNullable(source).orElse(new HashMap<K, V>());
      }
   
      public static <T> Iterator<T> of(Iterator<T> source) {
          return java.util.Optional.ofNullable(source).orElse(new ArrayList<T>().iterator());
      }
   
      public static <T> Collection<T> of(Collection<T> source) {
          return Optional.ofNullable(source).orElse(Lists.newArrayList());
      }
   
      public static <T> Iterable<T> of(Iterable<T> source) {
          return java.util.Optional.ofNullable(source).orElse(new ArrayList<T>());
      }
   
      public static <T> List<T> of(List<T> source) {
          return java.util.Optional.ofNullable(source).orElse(new ArrayList<T>());
      }
   
      public static <T> Set<T> of(Set<T> source) {
          return java.util.Optional.ofNullable(source).orElse(new HashSet<T>());
      }
   
      public static BigDecimal of(BigDecimal source) {
          return java.util.Optional.ofNullable(source).orElse(BigDecimal.ZERO);
      }
   
      public static BigDecimal toBigDecimal(String source, BigDecimal defaultValue) {
          Preconditions.checkNotNull(defaultValue);
          try {
              return new BigDecimal(StringUtils.trimToEmpty(source));
          } catch (Throwable t) {
              logger.warn("未能识别的boolean类型, source:{}", source, t);
              return defaultValue;
          }
      }
   
      public static int toInt(String source, int defaultValue) {
          if (StringUtils.isBlank(source)) {
              return defaultValue;
          }
          try {
              return Integer.parseInt(StringUtils.trimToEmpty(source));
          } catch (Throwable t) {
              logger.warn("未能识别的整形 {}", source);
              return defaultValue;
          }
      }
   
      public static long toLong(String source, long defaultValue) {
          if (StringUtils.isBlank(source)) {
              return defaultValue;
          }
          try {
              return Long.parseLong(StringUtils.trimToEmpty(source));
          } catch (Throwable t) {
              logger.warn("未能识别的长整形 {}", source);
              return defaultValue;
          }
      }
   
      public static boolean toBoolean(String source, boolean defaultValue) {
          if (StringUtils.isBlank(source)) {
              return defaultValue;
          }
          try {
              return Boolean.parseBoolean(StringUtils.trimToEmpty(source));
          } catch (Throwable t) {
              logger.warn("未能识别的boolean类型, source:{}", source, t);
              return defaultValue;
          }
      }
   
      public static void run(Runnable runnable, Consumer<Throwable> error) {
          try {
              runnable.run();
          } catch (Throwable t) {
              error.accept(t);
          }
      }
   
  }
  ```

* 有没有必要封装一层http？==监控，mock不同环境的数据（代码不全，仅供参考其实现思想）

  现在项目里面是基于boot的，不知道有没有一些监控或者转发之类的功能，以后测试环境机器多了，可以通过封装的http设置自己想要访问的mock数据源，同时用异步请求，并且用切面记录http的一些数据（时间啊，参数啊之类的）

  ```
  public ListenableFuture<String> post(HBaseQuery baseQuery,
                                       HosInterface hosInterface,
                                       Object param, Date timeStamp) {
      // 0. build a promise
      String hosCode = baseQuery.getHosCode();
      SettableFuture<String> result = SettableFuture.create();
      String url = hosInterface.dynamicRouter(hospitalService.load(hosCode));
      long enqueueStart = System.currentTimeMillis();
   
      // 1. build a processor
      Consumer<Runnable> processor = this.buildProcessor(hosCode,
              enqueueStart, baseQuery, param, timeStamp, url, hosInterface, result);
   
      // 2. acquire limiter in different queue
      limiters.get(hosInterface.limiterGroup)
              .acquire(System.currentTimeMillis(), calExpire(hosCode, hosInterface),
                      hosCode, hosInterface, true,
                      exceptionWorker(hosInterface.limiterGroup, hosCode, result), processor);
   
      return result;
  }
  ```

  ```
  private Consumer<Runnable> buildProcessor(String hosCode, long enqueueStart,
                                            HBaseQuery baseQuery, Object param,
                                            Date timeStamp, String url,
                                            HosInterface hosInterface, SettableFuture<String> result) {
      return release -> {
          Metrics.timer("hospitalAdaptor_enqueue_timer").tag("hos", hosCode).get()
                  .update(System.currentTimeMillis() - enqueueStart, TimeUnit.MILLISECONDS);
   
          // 2. build request
          String postData = format(baseQuery, param, timeStamp);
          HttpRequest request = RequestBuilder.createPost(url)
                  .setBody(postData).build();
   
          long start = System.currentTimeMillis();
          // 3. request async
          switchClient(hosInterface).asyncRequest(request, new AsyncHandlerBase() {
              @Override
              public void onRequestCompleted(AsyncHttpResponse asyncResponse) {
   
                  release.run();
                  String bodyAsString = asyncResponse.getBodyAsString();
                  long cost = System.currentTimeMillis() - start;
                  logger.info("schema: [{}-{}], url:[{}], request:[{}], response:[{}], cost:[{} ms]",
                          hosInterface, hosCode, url, compress(postData), compress(bodyAsString), cost);
                  if (InterfaceLimiterGroup.LIS == hosInterface.limiterGroup) {
                      Metrics.timer("postRequestToLis_succ_count")
                              .tag("hos", hosCode)
                              .tag("mode", hosInterface.mode.name())
                              .tag("interface", hosInterface.getMonitorTagName()).get()
                              .update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                  } else {
                      Metrics.timer("postRequestToHis_succ_count")
                              .tag("hos", hosCode)
                              .tag("mode", hosInterface.mode.name())
                              .tag("interface", hosInterface.getMonitorTagName()).get()
                              .update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                  }
                  // 处理非正常返回的请求
                  if (asyncResponse.getStatusLine().getStatusCode() != 200) {
                      StatusLine status = asyncResponse.getStatusLine();
                      if (needMonitor(baseQuery, hosInterface)) {
                          if (InterfaceLimiterGroup.LIS == hosInterface.limiterGroup) {
                              Metrics.timer("postRequestToLis_error_count").tag("hos", hosCode).tag("mode",
                                      hosInterface.mode.name())
                                      .tag("interface", hosInterface.getMonitorTagName()).get()
                                      .update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                          } else {
                              Metrics.timer("postRequestToHis_error_count").tag("hos", hosCode).tag("mode",
                                      hosInterface.mode.name())
                                      .tag("interface", hosInterface.getMonitorTagName()).get()
                                      .update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                          }
                      }
                      result.setException(new IllegalStateException(
                              MessageFormat.format("{0}, 对方接口响应码错误, 错误码:{1}, 响应信息:{2}",
                                      hosCode, status.getStatusCode(), status.getReasonPhrase())));
                      return;
                  }
   
                  // 监控返回码
                  monitorResponseCode(bodyAsString, hosCode, hosInterface);
   
                  result.set(bodyAsString);
              }
   
   
              @Override
              public void onThrowable(Throwable throwable) {
                  release.run(); // 释放信号量
                  logger.warn("schema: [{}-{}], url:[{}], request:[{}], response: error",
                          hosInterface, hosCode, url, compress(postData), throwable);
                  if (InterfaceLimiterGroup.LIS == hosInterface.limiterGroup) {
                      Metrics.timer("postRequestToLis_succ_count")
                              .tag("hos", hosCode)
                              .tag("mode", hosInterface.mode.name())
                              .tag("interface", hosInterface.getMonitorTagName()).get()
                              .update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                  } else {
                      Metrics.timer("postRequestToHis_succ_count")
                              .tag("hos", hosCode)
                              .tag("mode", hosInterface.mode.name())
                              .tag("interface", hosInterface.getMonitorTagName()).get()
                              .update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                  }
   
                  if (needMonitor(baseQuery, hosInterface)) {
                      if (InterfaceLimiterGroup.LIS == hosInterface.limiterGroup) {
                          Metrics.timer("postRequestToLis_error_count").tag("hos", hosCode).tag("mode",
                                  hosInterface.mode.name())
                                  .tag("interface", hosInterface.getMonitorTagName()).get()
                                  .update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                      } else {
                          Metrics.timer("postRequestToHis_error_count").tag("hos", hosCode).tag("mode",
                                  hosInterface.mode.name())
                                  .tag("interface", hosInterface.getMonitorTagName()).get()
                                  .update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                      }
                  }
   
                  result.setException(new IllegalStateException(throwable));
              }
          });
      };
  }
  ```

  ```
  public void acquire(long startTime, long expire, String hosCode, HosInterface hosInterface, boolean acquire,
                      Consumer<Throwable> reject, Consumer<Runnable> acquired) {
      if (!acquire) {//不需要限流
          acquired.accept(() -> {});
          return;
      }
   
      if (checkAboard(hosCode, hosInterface)) {
          reject.accept(new RejectedExecutionException(
                  MessageFormat.format("当前医院[{0}]接口[{1}]访问时队列已满", hosCode, hosInterface.name())));
          return;
      }
      // check expire
      // 请求等待超时一般发生在业务高峰期内
      long curTime = System.currentTimeMillis();
      if (curTime > expire) {
          // 等待时间格式，最多十来秒，精确到毫秒
          String waitTimeFormat = "ss.SSS";
          try {
              reject.accept(new RejectedExecutionException(
                      MessageFormat.format("当前医院[{0}]接口[{1}]等待超时, 累计等待时间:[{2}]",
                              hosCode, hosInterface.name(), DateFormatUtil.formatFree(new Date(curTime - startTime), waitTimeFormat))));
          } catch (Exception e) {
              logger.info("unexpected reject error", e);
              reject.accept(new RejectedExecutionException(
                      MessageFormat.format("当前医院[{0}]访问QPS过高, 等待超时, 该请求麻烦你拿回去重试", hosCode)));
          }
          return;
      }
   
      if(managedHttpService.isHosInterLimitOpen()) {
          Semaphore semaphore = this.tryAcquire(hosCode, hosInterface);
          if (semaphore != null) {
              Metrics.counter("his_concurrent_request").tag("hosCode", hosCode).get().inc();
              acquired.accept(() -> {
                  semaphore.release();
                  Metrics.counter("his_concurrent_request").tag("hosCode", hosCode).get().dec();
              });
              return;
          }
      } else {
          boolean success = this.getOrInitLimiter(hosCode).tryAcquire();
          if (success) {
              Metrics.counter("his_concurrent_request").tag("hosCode", hosCode).get().inc();
              acquired.accept(() -> {
                  this.limiters.get(hosCode).release();
                  Metrics.counter("his_concurrent_request").tag("hosCode", hosCode).get().dec();
              });
              return;
          }
      }
   
      AtomicLong counter = this.getOrInitCounter(hosCode, hosInterface);
      counter.incrementAndGet();
      // single sentry schedule thread
      XTimer.setTimeout(() -> {
          // 特别注意!!!! incr 和 decr之间不能有任何代码!!!
          // 如果修改了计数器的配置，则两次调用getOrInitCounter获取到的实例可能会不一样，所以这里需要维持counter的引用
          counter.decrementAndGet();
          acquire(startTime, expire, hosCode, hosInterface, true, reject, acquired);
      }, ACQUIRE_INTERVAL, TimeUnit.MILLISECONDS, MoreExecutors.directExecutor());
  }
  ```

* 统一返回值

  ResponseBodyAdvice

* 异步completableFuture（for循环网络请求）

  当然最好的方式就是支持批量查询，findUserData已经支持了，但是没有进行批量查询，而本话题其实主要想引用异步，所以先暂时只关注cpmpletableFuture

  首先 iamService.findUserData可以批量查询，我们for循环查询浪费了网络资源，如果for循环的话，切换成异步好一些

  ```
  orgCodes.stream().forEach(code -> {
      Map<String, List> users = new HashMap<>();
      roleInfos.stream().forEach(roleInfo -> {
          long starta = System.currentTimeMillis();
          BaseResponse userDataResponse = iamService.findUserData(code, Arrays.asList(roleInfo.getId()));
          log.info("单个项目人员查询耗时,{}",System.currentTimeMillis()-starta);
          if (userDataResponse.getCode().equals(ProjectConstant.REQUEST_SUCCESS_CODE) && userDataResponse.getData() != null) {
              List<IamV2UserRoleDTO> data = (List<IamV2UserRoleDTO>) userDataResponse.getData();
              if (users.containsKey(roleInfo.getRoleName())) {
                  users.get(roleInfo.getRoleName()).addAll(data);
              } else {
                  users.put(roleInfo.getRoleName(), data);
              }
          }
      });
      result.put(code, users);
  });
  ```

  改造成

  为什么不是list直接groupingby当userMap使用，因为构造users的对象是嵌套在roleInfos里面，需要两个map合并，否则数据不对

  ```
  orgCodes.stream().forEach(code -> {
              Map<String, List> users = new HashMap<>();
              //for http 换成completableFutures
              List<IamV2UserRoleDTO> list=new ArrayList<>();
              List<CompletableFuture<List<IamV2UserRoleDTO>>> completableFutureList=deal(code,roleInfos);
              if(CollectionUtils.isNotEmpty(completableFutureList)){
                  completableFutureList.stream().forEach(ee->{
                      try {
                          List<IamV2UserRoleDTO> ll=ee.get(5, TimeUnit.SECONDS);
                          list.addAll(ll);
                      } catch (Exception eeee) {
                          eeee.printStackTrace();
                      }
                  });
              }
              Map<String, List<IamV2UserRoleDTO>> userMap = list.stream().collect(groupingBy(IamV2UserRoleDTO::getRoleName));
   
              Map<String, List<IamV2UserRoleDTO>> roleMap = roleInfos.stream().collect(toMap(RoleInfo::getRoleName, d -> new ArrayList()));
   
              Map<String, List<IamV2UserRoleDTO>> collect = Stream.of(roleMap, userMap).flatMap(x -> x.entrySet().stream()).collect(toMap(Map.Entry::getKey,Map.Entry::getValue,
                      (value1, value2) -> {
                          value1.addAll(value2);
                          return value1;
                      }));
   
  //            roleMap.forEach((key,value)->userMap.merge(key,value,(v1,v2)->{v1.addAll(v2);return v1;}));
               
              result.put(code, userMap);
          });
   
  private List<CompletableFuture<List<IamV2UserRoleDTO>>> deal(String code,List<RoleInfo> roleInfos) {
      List<CompletableFuture<List<IamV2UserRoleDTO>>> completableFutureList=new ArrayList<>();
      roleInfos.stream().filter(Objects::nonNull).forEach(ee->{
          completableFutureList.add(getDataFuture(code,ee.getId()));
      });
      return completableFutureList;
  }
   
  private CompletableFuture<List<IamV2UserRoleDTO>> getDataFuture(String code,String id) {
      return CompletableFuture.supplyAsync(()->getData(code,id),
              new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors()*2,Runtime.getRuntime().availableProcessors()*2+1,3,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(10),r->new Thread(r,"123")));
  }
   
  private List<IamV2UserRoleDTO> getData(String code,String id) {
      BaseResponse userDataResponse = iamService.findUserData(code, Arrays.asList(id));
      return (List<IamV2UserRoleDTO>)userDataResponse.getData();
  }
  ```

  如果是单个http请求，可以这么处理

  ```
  public static void supplyAsync() throws Exception {        
      CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
          try {
              //业务
          } catch (InterruptedException e) {
          }
         
          return System.currentTimeMillis();
      });
   
      long time = future.get();
  }
  ```

* 没有返回值的方法扔到线程池里面去执行

  submit返回future，execute返回void

  ```
  ExecutorService executorService=new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors()*2,Runtime.getRuntime().availableProcessors()*2+1,3,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(10),r->new Thread(r,"123"))；
   
  Safes.of(list).forEach(xxx ->
          executorService.submit(() -> crawl0(xxx)));
  ```

* 并行====parallel或者countDownLatch（for循环本地数据）

  ```
  CountDownLatch countDownLatch = new CountDownLatch(files.length);
  ExecutorService tp = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2 + 1, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), r -> new Thread(r, "123"));
  List<Map<String, Goods>> goodsList = new CopyOnWriteArrayList<Map<String, Goods>>();
   
  Lists.newArrayList(files).stream().forEach(file -> tp.execute(() -> {
      Map<String, Goods> expensive5Goods = getExpensive5Goods(file);
      goodsList.add(expensive5Goods);
      countDownLatch.countDown();
  }));
  countDownLatch.await();
  tp.shutdown();
   
  或者
  List<Map<String, Goods>> goodsList = Arrays.stream(files).parallel().map(RRFiles::getExpensive5Goods).collect(Collectors.toList());
  ```

* sql拆分，表结构字段冗余设置

  1.梳理接口进行优化，去掉left join，只用简单sql

  2.数据库设计，部分字段冗余(迁移部分表结构字段)

  3.双写数据，重构数据库表结构的依赖关系?  reference：https://quericy.me/blog/867/

  4.数据库可以把一些非索引的字段存到一个json对象作为一个字段扔到数据库里面，防止频繁的ddl字段变更

  ```
  //业务调用，或者traceinfo的值
  TraceInfo.Key.multipleParams.get(context.getOrder().getTraceInfo())
  //业务调用，设置traceinfo的值
  TraceInfo.Key.multipleParams.put(candidateOrder.getMainOrder().getTraceInfo(), TraceInfo.Key.multipleParams.name());
  ```

  ```
  package com.longfor.plm.project.util;
   
  import com.google.common.collect.Maps;
  import lombok.Data;
   
  import java.io.Serializable;
  import java.util.Map;
  import java.util.Optional;
  @Data
  public class TraceInfo implements Serializable {
   
      /**
       * 机器编码
       */
      private String machineCode;
      /**
       * 微信下单的openId
       */
      private String openId;
   
      /**
       * app下单的cid
       */
      private String cid;
   
      /**
       * 扩展信息字段
       */
      private Map<String, String> extra;
   
      /**
       * 扩展字段 key
       */
      public enum Key implements TracePut {
          contactIDCardType("联系人证件类型"),
          contactIDCardNo("联系人证件号码"),
          addressInBeijing("在京住址")
          ;
   
          @Override
          public String get(TraceInfo traceInfo) {
              if (traceInfo == null || traceInfo.getExtra() == null) {
                  return null;
              }
              return traceInfo.getExtra().get(this.name());
          }
   
          @Override
          public void put(TraceInfo traceInfo, String value) {
              Map<String, String> extra = Optional.ofNullable(traceInfo.getExtra()).orElse(Maps.newHashMap());
              extra.put(this.name(), value);
              traceInfo.setExtra(extra);
          }
   
          private String desc;
   
          Key(String des) {
          }
      }
   
   
  }
   
  interface TracePut {
      String get(TraceInfo traceInfo);
   
      void put(TraceInfo traceInfo, String value);
  }
  ```

* mybtis分页插件ipagehelper（已经存在代码中），mybatis生成代码插件gengerator

  pagehelper取代page信息在代码里面赋值

  generator自动生成dao，mapper，dto（https://blog.csdn.net/qq_43583597/article/details/89294963），因为通用mapper大家有可能不是很熟悉

  ```
  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE generatorConfiguration
          PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
          "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
  <generatorConfiguration>
      <!-- 数据库驱动:选择你的本地硬盘上面的数据库驱动包-->
      <classPathEntry  location="D:\lhproject\mysql-connector-java-5.1.45-bin.jar"/>
      <context id="DB2Tables"  targetRuntime="MyBatis3">
          <commentGenerator>
              <property name="suppressDate" value="true"/>
              <!-- 是否去除自动生成的注释 true：是 ： false:否 -->
              <property name="suppressAllComments" value="true"/>
          </commentGenerator>
          <!--数据库链接URL，用户名、密码 -->
          <jdbcConnection driverClass="com.mysql.jdbc.Driver" connectionURL="jdbc:mysql://10.231.129.164:3306/plm-project-uat" userId="plmuser" password="vIB1i@Rau7MMDm!M">
          </jdbcConnection>
          <javaTypeResolver>
              <property name="forceBigDecimals" value="false"/>
          </javaTypeResolver>
          <!-- 生成模型的包名和位置-->
          <javaModelGenerator targetPackage="com.longfor.plm.project.testGenerator.entity" targetProject="src/main/java">
              <property name="enableSubPackages" value="true"/>
              <property name="trimStrings" value="true"/>
          </javaModelGenerator>
          <!-- 生成映射文件的包名和位置-->
          <sqlMapGenerator targetPackage="main.resources.test.mapping" targetProject="src">
              <!-- enableSubPackages:是否让schema作为包的后缀 -->
              <property name="enableSubPackages" value="false" />
          </sqlMapGenerator>
          <!-- 生成DAO的包名和位置-->
          <javaClientGenerator type="XMLMAPPER" targetPackage="com.longfor.plm.project.testGenerator.dao" targetProject="src/main/java">
              <property name="enableSubPackages" value="true"/>
          </javaClientGenerator>
          <!-- 要生成的表 tableName是数据库中的表名或视图名 domainObjectName是实体类名-->
          <table tableName="plan_info"
                 domainObjectName="PlanInfo"
                 enableCountByExample="false"
                 enableUpdateByExample="false"
                 enableDeleteByExample="false"
                 enableSelectByExample="false"
                 selectByExampleQueryId="false">
   
          </table>
   
      </context>
  </generatorConfiguration>
  ```

  



