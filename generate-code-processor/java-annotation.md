

# 内置注解
Built-In Java Annotations used in Java code
1. @Override
2. @SuppressWarnings
3. @Deprecated

Built-In Java Annotations used in other annotations
1. @Target
2. @Retention
3. @Inherited
4. @Documented
5. @Repeatable (java 8)

另一种区分注解的纬度
1. Marker Annotation
2. Single-Value Annotation
3. Multi-Value Annotation

## 关于 @Target

|Element Types| Where the annotation can be applied|
|:------| :------ |
|TYPE	|class, interface or enumeration|
|FIELD	|fields|
|METHOD	|methods|
|CONSTRUCTOR	|constructors|
|LOCAL_VARIABLE	|local variables|
|ANNOTATION_TYPE	|annotation type|
|PARAMETER	|parameter|

Example to specify annoation for a class
```java 
@Target(ElementType.TYPE)  
@interface MyAnnotation{  
int value1();  
String value2();  
}  
```

## 关于 @Retention
@Retention annotation is used to specify to what level annotation will be available.

|RetentionPolicy	|Availability|
|:------| :------ |
|RetentionPolicy.SOURCE	|refers to the source code, discarded during compilation. It will not be available in the compiled class.|
|RetentionPolicy.CLASS	|refers to the .class file, available to java compiler but not to JVM . It is included in the class file.|
|RetentionPolicy.RUNTIME	|refers to the runtime, available to java compiler and JVM .|


|级别	|技术	|说明  |保留期
|:------| :------ |:------ |:------ |
|SOURCE源码级别	|APT	|在编译期能够获取注解与注解声明的类，包括类中的成员信息，一般用于生成额外的辅助类。| 只保留在源码里。class文件里没有。only in the source code ando not in the .class file or at runntime .used with build-tools that sacn the file|
|CLASS字节码	|字节码插桩	|在编译生成class之后，根据注解作为判断，通过修改class的数据来实现字节码级别的插桩操作|存在class文件里，但反射不到.stored in the .class file  & not available at the runntime|
|RUNTIME运行时	|反射	|在程序运行期间，通过反射技术动态获取注解与属性元素值，来完成逻辑操作。|存在class文件里，可以反射。available via reflection at runtime｜

https://blog.csdn.net/oman001/article/details/106041869

相关依赖library: org.ows2.asm:asm:7.1   org.ows2.asm:asm-commons:7.1

Example to specify the RetentionPolicy
```java 
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.TYPE)  
@interface MyAnnotation{  
int value1();  
String value2();  
}
```  

## 关于 @Repeatable

java8 引入的特性，一个对象可以有多个注解。

以下是示例：
你需要一个定时任务，定时执行doPeriodicCleanup函数。
一个是每周五下午11点执行，一个是每个月的月末执行。可以用这个特性。

定义注解：

```java 
import java.lang.annotation.Repeatable;

@Repeatable(Schedules.class)
public @interface Schedule {
  String dayOfMonth() default "first";
  String dayOfWeek() default "Mon";
  int hour() default 12;
}
```

使用：
```java 
@Schedule(dayOfMonth="last")
@Schedule(dayOfWeek="Fri", hour="23")
public void doPeriodicCleanup() { ... }
```  


# Questions
 
1. 这个接口那里错了，或者不好。
```java 
public interface House {
    @Deprecated
    void open();
    void openFrontDoor();
    void openBackDoor();
}
```
2.  接着看上一个例子的实现：
```java 
public class MyHouse implements House {
    public void open() {}
    public void openFrontDoor() {}
    public void openBackDoor() {}
}
```
如果我们编译这个程序，产生了一个警告，我们怎么把这个警告消掉。
 

3.下面的代码有编译错误码？如果有怎么改好？


```java 
public @interface Meal { ... }

@Meal("breakfast", mainDish="cereal")
@Meal("lunch", mainDish="pizza")
@Meal("dinner", mainDish="salad")
public void evaluateDiet() { ... }
```


Answer  1
```java 
public interface House { 
    /**
     * @deprecated use of open 
     * is discouraged, use
     * openFrontDoor or 
     * openBackDoor instead.
     */
    @Deprecated
    public void open(); 
    public void openFrontDoor();
    public void openBackDoor();
}
```

Answer  2
```java 
public class MyHouse implements House { 
    @SuppressWarnings("deprecation")
    public void open() {} 
    public void openFrontDoor() {}
    public void openBackDoor() {}
}
```
Answer  3
```java 
@java.lang.annotation.Repeatable(MealContainer.class)
public @interface Meal { ... }

public @interface MealContainer {
    Meal[] value();
}
```
