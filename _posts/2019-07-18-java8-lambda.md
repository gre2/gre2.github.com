---
layout: post
title: "lambda"
description: ""
category: [java,基础]
tags: [基础]
---
{% include JB/setup %}



# list，map互转

### list转map对象

```java
Map<String, List<Condition>> map1 = conditions.stream().collect(Collectors.groupingBy(Condition::getCondName));
```

```java
{name3=[Condition(condName=name3, condValue=[val31, val32, val33])], name2=[Condition(condName=name2, condValue=[val21])], name1=[Condition(condName=name1, condValue=[val11, val12])]}
```

### list转map对象，抽取特殊属性列表

```java
Map<String, List<List<String>>> map2 = conditions.stream().collect(Collectors.groupingBy(Condition::getCondName, Collectors.mapping(Condition::getCondValue, Collectors.toList())));
```

```java
{name3=[[val31, val32, val33]], name2=[[val21]], name1=[[val11, val12]]}
```

### list转map对象，key相同value覆盖

```java
Map<Long, User> maps = userList.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));
```

```java
Map<Long, String> maps = userList.stream().collect(Collectors.toMap(User::getId, User::getAge);
```

### map转list

```java
List<CandidateInfo> list=map.entrySet().stream().map(entry->entry.getValue()).flatMap(List::stream).collect(Collectors.toList());
```



# lamdba代码应用

### 实现Runnable===用()->{}替换匿名类===线程，事件监听，排序

* 线程

```java
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

这个例子向我们展示了java8 lambda表达式的语法

```java
(params) -> expression
(params) -> statement
(params) -> { statements }
```

如果你的方法不对参数进行修改和重写，只是在控制台打印点东西的话

```java
() -> System.out.println("Hello Lambda Expressions");
```

如果你的方法接收两个参数，可以写成

```java
(int even, int odd) -> even + odd
```

* 事件监听

```java
// Java 8之前：
JButton show =  new JButton("Show");
show.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
    System.out.println("Event handling without lambda expression is boring");
    }
});

// Java 8方式：
show.addActionListener((e) -> {
    System.out.println("Light, Camera, Action !! Lambda expressions Rocks");
});
```

### lamdba对列表进行迭代===方法引用===类:::方法名

```java
// Java 8之前：
List features = Arrays.asList("Lambdas", "Default Method", "Stream API", "Date and Time API");
for (String feature : features) {
    System.out.println(feature);
}

// Java 8之后：
List features = Arrays.asList("Lambdas", "Default Method", "Stream API", "Date and Time API");
features.forEach(n -> System.out.println(n));
 
// 使用Java 8的方法引用更方便，方法引用由::双冒号操作符标示，
// 看起来像C++的作用域解析运算符
features.forEach(System.out::println);
```

### lamdba表达式和函数式接口Predicate

除了在语言层面支持函数式编程风格，java8页添加了一个包，叫做java.util.function，它包含了很多类，来支持函数式编程，其中一个便是Predicate，用此可以向API方法添加逻辑，用更少的代码支持更多的动态行为。

```java
public static void main(args[]){
    List languages = Arrays.asList("Java", "Scala", "C++", "Haskell", "Lisp");
 
    System.out.println("Languages which starts with J :");
    filter(languages, (str)->str.startsWith("J"));
 
    System.out.println("Languages which ends with a ");
    filter(languages, (str)->str.endsWith("a"));
 
    System.out.println("Print all languages :");
    filter(languages, (str)->true);
 
    System.out.println("Print no language : ");
    filter(languages, (str)->false);
 
    System.out.println("Print language whose length greater than 4:");
    filter(languages, (str)->str.length() > 4);
}
 
public static void filter(List names, Predicate condition) {
    names.stream().filter((name) -> (condition.test(name))).forEach((name) -> {
        System.out.println(name + " ");
    });
}
```

java.util.function.Predicate 允许将两个或更多的 Predicate 合成一个。它提供类似于逻辑操作符AND和OR的方法，名字叫做and()、or()和xor()，用于将传入 filter() 方法的条件合并起来。例如，要得到所有以J开始，长度为四个字母的语言，可以定义两个独立的 Predicate 示例分别表示每一个条件，然后用 Predicate.and() 方法将它们合并起来。

```java
Predicate<String> startsWithJ = (n) -> n.startsWith("J");
Predicate<String> fourLetterLong = (n) -> n.length() == 4;
names.stream()
    .filter(startsWithJ.and(fourLetterLong))
    .forEach((n) -> System.out.print("nName, is : " + n));
```

### lamdba之map和reduce

```java
// 不使用lambda表达式为每个订单加上12%的税
List costBeforeTax = Arrays.asList(100, 200, 300, 400, 500);
for (Integer cost : costBeforeTax) {
    double price = cost + .12*cost;
    System.out.println(price);
}
 
// 使用lambda表达式
List costBeforeTax = Arrays.asList(100, 200, 300, 400, 500);
costBeforeTax.stream().map((cost) -> cost + .12*cost).forEach(System.out::println);
```

```java
// 为每个订单加上12%的税
// 老方法：
List costBeforeTax = Arrays.asList(100, 200, 300, 400, 500);
double total = 0;
for (Integer cost : costBeforeTax) {
    double price = cost + .12*cost;
    total = total + price;
}
System.out.println("Total : " + total);
 
// 新方法：
List costBeforeTax = Arrays.asList(100, 200, 300, 400, 500);
double bill = costBeforeTax.stream().map((cost) -> cost + .12*cost).reduce((sum, cost) -> sum + cost).get();
System.out.println("Total : " + bill);
```

### lambda之filter

```java
List<String> filtered = strList.stream().filter(x -> x.length()> 2).collect(Collectors.toList());
```

原集合不变，获取一个新列表

### lambda之collect

```java
String G7Countries = G7.stream().map(x -> x.toUpperCase()).collect(Collectors.joining(", "));
```

### lamdba计算最大，最小，总和，平均值

IntStream、LongStream 和 DoubleStream 等流的类中，有个非常有用的方法叫做 summaryStatistics() 。可以返回 IntSummaryStatistics、LongSummaryStatistics 或者 DoubleSummaryStatistic s，描述流中元素的各种摘要数据。在本例中，我们用这个方法来计算列表的最大值和最小值。它也有 getSum() 和 getAverage() 方法来获得列表的所有元素的总和及平均值。

```java
//获取数字的个数、最小值、最大值、总和以及平均值
List<Integer> primes = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);
IntSummaryStatistics stats = primes.stream().mapToInt((x) -> x).summaryStatistics();
System.out.println("Highest prime number in List : " + stats.getMax());
System.out.println("Lowest prime number in List : " + stats.getMin());
System.out.println("Sum of all prime numbers : " + stats.getSum());
System.out.println("Average of all prime numbers : " + stats.getAverage());
```

### lambda和匿名类的比较

* 一个关键的不同点就是关键字 this。匿名类的 this 关键字指向匿名类，而lambda表达式的 this 关键字指向包围lambda表达式的类。
* 另一个不同点是二者的编译方式。Java编译器将lambda表达式编译成类的私有方法。使用了Java 7的 invokedynamic 字节码指令来动态绑定这个方法。