# Что это

Это примеры реализации классов-провайдеров пропертей из YAML файла.
Всего их 3 штуки-примера:
- Через аннотацию Value
- Через аннотацию ConfigurationProperty
- Через доставание из Environment ручками

# Почему несколько способов

Каждый способ имеет свои сильные и слабые стороны, К примеру:
- Value: достает из контекста и инжектит значения через рефлексию, что позволяет создать для полей только геттеры без сеттеров, таким образом,
поля по умолчанию readonly. Однако Value не умеет в чтение массивов из yml, для этого нужно ухищряться: 
вводить все значения в виде строки через разделитель и средствами SpEL получать список.
- ConfigurationProperties по префиксу маппит всю вложенную иерархию на класс по сопоставлению имя_проперти -> имя_переменной, умеет 
обрабатывать массивы yml и без ухищрений загоняет их в List. Однако к имени префикса есть требования, нельзя создавать
ещё бины с тем же префиксом - т.е. провайдер должен быть уникальным, а также поля должны иметь сеттеры, либо иметь конструктор
со всеми параметрами
Также автоматически проводит валидацию DTO по аннотациям.
- Использование объекта Environment и ручное извлечение пропертей оттуда: позволяет делать это в конструкторе, таким образом, 
можно сделать валидации значений на этапе создания объекта и не создавать его при невалидных. Так как достаем сами, поля
могут иметь только геттеры и быть readonly из коробки.

# Небольшие заметки

## Чтение из стороннего yml

Спринг по умолчанию не умеет читать из стороннего yml, кроме application.yml.
Чтобы смог, нужно создать фактори-класс и указать в PropertySource аннотации какогой-нибудь конфигурации

Шаг 1: создаем класс вида:
```java
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource)
            throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        Properties properties = factory.getObject();

        return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
    }
}
```

Шаг 2: указываем его в PropertySource: 
```java
@Configuration
@PropertySource(value = "classpath:my-custom-props.yml", factory = org.param_provider.config.YamlPropertySourceFactory.class)
```

## Логгирование значений пропертей в провайдере и их валидация

Для логгирования можно использовать `@PostConctruct` аннотацию и логгировать в её теле. Метод, помеченный этой 
аннотацией вызовется на этапе пост-обработки бина. Валидации можно запихнуть туда же, т.к. на этапе вызова контекст всё 
ещё не полностью поднят

Валидация для ConfigurationProperties по аннотациям Min/Max и т.д. срабатывает автоматически. 

В случае Value и Environment для валидации можно заинжектить бин интерфейса Validator и поместить в него текущий класс для 
проверки соблюдения правил, созданных аннотациями валидации.
Пример можно посмотреть тут: https://www.baeldung.com/spring-service-layer-validation
Более гибкие валидации можно сделать на уровне кода в том же `PostConstruct`.