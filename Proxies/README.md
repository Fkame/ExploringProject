# Что это такое: CGLIB, JDK dynamic proxy, Spring CGLIB

Модуль для изучения основных механизмов проксирования + поверхностно, как их использует спринг.
AspectJ не рассматривается из-за своей сложности и объема, тут только cglib, spring cglib и JDK dynamic proxy.

## Ссылки

Оф дока про JDK dynamic proxy: https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html
Про cglib: https://www.baeldung.com/cglib

Примеры кода cglib и jdk dynamic proxy:
https://dev.to/anh_trntun_4732cf3d299/dynamic-proxy-in-spring-a-comprehensive-guide-with-examples-and-demos-4kgd

Про aop proxy в спринге:
https://docs.spring.io/spring-framework/reference/core/aop/proxying.html

Пример использования ProxyFactory: https://docs.spring.io/spring-framework/reference/core/aop/proxying.html#aop-understanding-aop-proxies

## Оглавление

Основа из коробки - JDK dynamic proxy - [JDK dynamic proxy](JDK%20dynamic%20proxy.md)

Более прокачанный вариант в виде библиотеки - [CGLIB](CGLIB.md)

Обёртка в спринге, с которой это полегче, и которая используется самим спрингом - [Spring proxy](Spring%20proxy.md)

## Общие мысли

Прокси в Java это, в первую очередь, не паттерн Proxy - а механизм на уровне языка. Его отличает то, что он универсальный из-за напичканности рефлексией - это делает хендлер универсальным для +- любых классов при условии, что там нет какой-то специфической для тех или иных методов логики. Ну типа логгирование или транзакции - это +- универсально.

Если сравнивать с паттерном декоратор - тот для каждого декорируемого класса отдельный, адаптирован под специфику работы оного, и не универсален сам по себе, а привязан к предметке декорируемого класса, являясь, как бы, наслоением на его логику.

Также становится понятно, что лучше не создавать final классы без интерфейсов, т.к. их не получится запроксировать, т.е. в спринге они будут с обрезанными возможностями и нужно очень осмысленно тыкать final на класс.