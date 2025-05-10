# Подключение спринга через parent и через dependency pom import

Spring предоставляет не только целевые стартеры, к примеру spring-boot-starter-web или spring-boot-starter-actuator, но и специальные стартеры, в которых в dependencyManager задаются версии зависимостей ,а также настраиваются плагины.

Если их подключить, то в основном проекте останется только указать нужные зависимости не думая о зависимостях, т.к. их задание и совместимости уже предусмотрены спрингом.

Заиспользовать такие стартеры можно двумя способами:
1. Подключить spring-boot-starter-parent как родитель своего pom. Такой вариант осуществляет генератор проектов Spring Initializr
2. Если родитель уже занят, можно подключить не как родителя в скоупе import
# 1 -- Подключение стартера Spring как parent

Пример такого pom:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <artifactId>SpringAsParent</artifactId>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

Ключевые моменты:
1. Родительский pom это spring-boot-starter-parent и для него задаем версию
2. Зависимости подключаем без указания версии, т.к. их задаст dependencyManager в родительском стартере

>[!WARNING]
>В модульных проектах, когда подключаем родителя во вложенном модуле, который было решено сделать обособленным, нужно указать <relativePath/>, чтобы Maven не искал родителя среди локальных файлов и не выдавал ошибку:
>
>myRepo
>|- module1
>|- mySpringStandaloneModule
>
>При этом, в тэге module родительского pom нужно всё-равно указать имя модуля

# 2 -- Подключение стартера Spring в виде зависимости

В больших проектах часто применяется модульная структура и родителем модуля выступает модуль побольше. Так как родителя можно указать лишь одного, могут быть проблемы с подключением спринга в качестве родителя, если изначально он не предусматривался глобально. В этом случае, можно всё-равно подучить его возможности

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.example</groupId>
        <artifactId>ExporingProject</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>SpringDependenciesBom</artifactId>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>>spring-boot-starter-parent</artifactId>
                <version>3.4.5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

Ключевые моменты:
1. Родителем данного модуля выступает ExporingProject
2. spring-boot-starter-parent задается в dependencyManager с type = pom и scope = import
3. Зависимости подключаем без указания версии, т.к. их задаст dependencyManager в родительском стартере
# Что это за зависимости, в чём разница и какие нюансы

Несколько слов про эти стартеры-родители.
## spring-boot-starter-parent

Сам по себе занимается только настройкой плагинов, профилей, билдов, а dependencyManager содержится в  spring-boot-dependencies, которую он использует как родителя:
```xml
<parent>  
  <groupId>org.springframework.boot</groupId>  
  <artifactId>spring-boot-dependencies</artifactId>  
  <version>3.4.5</version>  
</parent>  
<artifactId>spring-boot-starter-parent</artifactId>
```

Если нам нужны и заверсионированные зависимости, и настроенные плагины мавена - используем данный стартер
От него нам достаются такие профили maven как:
- native
- nativeTest

В частности, его конфиги модифицируют параметры запуска приложения, к примеру, в этом фрагменте:
```xml
<plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <configuration>
            <parameters>true</parameters>
          </configuration>
</plugin>
```
Тут при компиляции добавится параметр `-parameter`, который позволит ,к примеру, spring-web маппить RequestParam и PathVariable с именем переменной без явного указания этого имени в самой аннотации.
Без такой настройки - будет ошибка в рантайме
## spring-boot-dependencies

Содержит в себе нереально большой dependencyManagement с кучей преднастроенных на совместимость зависимостей. Нам остается лишь их подключать в проект и готово.
Также он задает и версионирует пачку плагинов наподобие liquibase, flyway, kotlin, но не занимается такой настройкой мавеновских плагинов, как spring-boot-starter-parent

Если используются кастомные настройки мавеновских плагинов, подключать имеет смысл именно эту зависимость тогда.

# Манипулирование версиями с помощью properties

spring-boot-dependencies задает версии зависимостей в виде пропертей:
```xml
<properties>  
  <activemq.version>6.1.6</activemq.version>  
  <angus-mail.version>2.0.3</angus-mail.version>  
  <artemis.version>2.37.0</artemis.version>  
  <aspectj.version>1.9.24</aspectj.version>  
  <assertj.version>3.26.3</assertj.version>
...
```
Которые можно переопределить в своём проекте. Таким образом, нет необходимости подключать какую-то зависимость и явно ей тыкать версию, можно просто переопределить эту пропертю в своём pom