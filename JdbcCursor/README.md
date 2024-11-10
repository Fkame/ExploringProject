# Что это
Это модуль с примером удачного и неудачного курсорного чтения с помощью JDBC Template.

# Навигация

- В тестовом классе `CursorReadingIntegrationTest` находится пример удачного и неудачного чтения курсором с
ислледованием логов в ассертах. Это для удобства, чтобы было понятно, что искать.
- В `PostgresDataSourceConfiguration` создается датасурс для подключения к базе постгреса
- В `DbConfiguration` создается JdbcTemplate бин.

В папке `main/java/resources` лежит application.yml с пропертями для коннекта к БД, а также docker-compose файл, 
чтобы поднять себе postgres с настроенными коннектами локально.
Также там лежит init.sql файл, который считает postgresq при первом поднятии и создаст схему + таблицу, чтобы не делать это руками.


В папке `test/java/resources` лежит application.yml с дубликатом коннектов к БД (ну мало ли), и там же настроены уровни логгирования
на TRACE, чтобы было удобно изучать логи после тестов.

# Как воспроизвести

1. Раскатить лежащий `main/java/resources/db/docker-compose.yml`. Командой `docker-compose up` из терминала из папки с файлом. Т.е.
```cmd
cd <your-folder>/main/java/resources/db
docker-compose up
```

2. Когда он раскатится, на `localhost:9100` будет доступен pgAdmin, чтобы просматривать саму базу. Коннект нужно будет в нём
настроить руками. Единственное, вместо localhost в pgAdmin при настройке подключения к БД нужно указать `host.docker.internal` -
т.к. каждый контейнер в своём сетевом пространстве и нужно мост докера использовать, чтобы один мог попасть в другой.
3. Сама база доступна по `localhost:5432`.
4. Далее можно запускать тесты и изучать логи для понимания работы курсорного чтения.

# Теоретическая справка (небольшая)

**Документация и примеры от Postgres по курсорному чтению:** https://jdbc.postgresql.org/documentation/query/

## Условия срабатывания курсорного чтения

Чтобы открылся курсор и мы смогли получать от него данные есть 2 важных условия:
1. Должен быть установлен fetchSize параметр. Он указывает на то, сколько строк мы хотим получать за один fetch-вызов (fetch это сигнал базе, что мы готовы получить ещё фрагмент данных).
2. Параметр autoCommit должен быть установлен в false. Этот параметр в чистом JDBC означает транзакцию. Если он установлен в true - 
каждый запрос выполняется обособленно, без транзакции. Если он установлен в false - то считается, что несколько запросов выполняются в транзакции, 
пока не будет явно вызван COMMIT (подтверждение транзакции) или ROLLBACK (откат транзакции).

Если хоть одно из условий нарушается - курсорного чтения не будет, а драйвер просто выгрузит все строки в оперативную память приложения и будет читать оттуда.
Эта ситуация очень тяжело отслеживается, но по логам это можно понять.

## Как происходит чтение курсором в чистом JDBC (пример из статьи постгресовцев)
Они приводят вот такой фрагмент кода:
```java
// make sure autocommit is off
conn.setAutoCommit(false);
Statement st = conn.createStatement();

// Turn use of the cursor on.
st.setFetchSize(50);
ResultSet rs = st.executeQuery("SELECT * FROM mytable");
while (rs.next()) {
    System.out.print("a row was returned.");
}
rs.close();

// Close the statement.
st.close();
```

В нём видно, что устанавливается autoCommit в false, т.е. открывается транзакция, далее выставляется fetchSize - размер считываемого фрагмента. Предусловия чтения курсором соблюдены.

**Само чтение выполняется с помощью итерирования по ResultSet.**
rs.next() возвращает boolean - была ли получена строка или нет. Если вернет false - всё было прочитано, если вернет true - был произведен сдвиг на следующую строку из БД. Из-за этого вызов помечается в цикл, т.к. он двигает курсор.

Таким образом, при курсорном чтении мы итерируемся по ResultSet, получая строки от него.

На самом деле, выполняя .next() мы не можем знать, это данные из оперативной памяти, или он ходит в БД. Однако, если чтение всё-таки курсорное, под капотом ResultSet кэширует полученные фрагменты из БД, т.е. это работает так:
1. Мы вызываем next(), говоря ResultSet, чтобы он подготовил следующую строку для обработки
2. Если в кэше нет строк - вызывается метод fetch - resultSet получает фрагмент строк из БД и кэширует его. Если было получено 0 строк, мы достигли конца - возвращается false, иначе ResultSet переводит указатель на первую полученную строку и возвращает true.
3. Если в кэше есть строки - он просто двигает указатель на новую и возвращает true.

Полный пример в статье выглядит как 2 запроса: первый курсорный с выставленным fetchSize, второй некурсорные, с выставленным fetchSize = 0, т.е. получаем все строки за раз:
```java
// make sure autocommit is off
conn.setAutoCommit(false);
Statement st = conn.createStatement();

// Turn use of the cursor on.
st.setFetchSize(50);
ResultSet rs = st.executeQuery("SELECT * FROM mytable");
while (rs.next()) {
    System.out.print("a row was returned.");
}
rs.close();

// Turn the cursor off.
st.setFetchSize(0);
rs = st.executeQuery("SELECT * FROM mytable");
while (rs.next()) {
    System.out.print("many rows were returned.");
}
rs.close();

// Close the statement.
st.close();
```
## Как реализовать с помощью JdbcTemplate
JdbcTemplate это обёртка над JDBC в Java, автоматизирующий процесс выполнения запроса и получения resultSet, а также извлечения строк из него.

В JdbcTemplate помещается DataSource, в следствие чего, объекты коннекта он получает из него, а запросы передаются просто текстом. Закрывает он также их сам, чтобы мы об этом не думали.

Пример курсорного чтения через JdbcTemplate в Spring:
```java
@Transactional
public void readAll() { 

	// Чисто для примера создания datasource
	PGSimpleDataSource dataSource = new PGSimpleDataSource();  
	dataSource.setUrl(postgresPropertiesProvider.getUrl());  
	dataSource.setUser(postgresPropertiesProvider.getUsername());  
	dataSource.setPassword(postgresPropertiesProvider.getPassword());  
	dataSource.setCurrentSchema(postgresPropertiesProvider.getSchema());

	JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	jdbcTemplate.setFetchSize(10);

    jdbcTemplate.query("SELECT * FROM my_table", rs -> {  
                if (rs.isClosed()) {  
                    throw new RuntimeException("ResultSet was closed!");  
                }  
  
                // Вызов этого метода каждый fetchSize раз должен вызывать .fetch и открытие портала постгреса для дотягивания значений  
                while (rs.next()) {  
					// тут всякие вызовы rs.getInt / rs.getObject для маппинга и логика обработки строк
                }                
                return null;  
            }  
    );  
}
```

## Логи когда курсор работает как надо

При курсорном чтении несколько раз вызывается метод fetch, который порождает повторяющуюся строку с текстом:
```
Execute(portal=C_3,limit=2)
```
То есть мы пытаемся получить 2 записи из портала, т.к. fetchSize был установлен = 2.

Текст:
```
 DataRow(len=47)
```
Говорит о том, что мы получили строку, их в логах по 2, т.е. 2 строки было получено за 1 вызов fetch.

Полный фрагмент таких логов:
```logs
2024-11-10T21:12:18.722+03:00 DEBUG 32140 --- [           main] o.s.jdbc.core.JdbcTemplate               : Executing SQL query [SELECT * FROM test_table]
2024-11-10T21:12:18.723+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :   simple execute, handler=org.postgresql.jdbc.PgStatement$StatementResultHandler@5eed6dfb, maxRows=0, fetchSize=2, flags=9
2024-11-10T21:12:18.723+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Parse(stmt=null,query="SELECT * FROM test_table",oids={})
2024-11-10T21:12:18.723+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Bind(stmt=null,portal=C_3)
2024-11-10T21:12:18.723+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Describe(portal=C_3)
2024-11-10T21:12:18.724+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Execute(portal=C_3,limit=2)
2024-11-10T21:12:18.724+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Sync
2024-11-10T21:12:18.725+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE ParseComplete [null]
2024-11-10T21:12:18.725+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE BindComplete [C_3]
2024-11-10T21:12:18.725+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE RowDescription(3)
2024-11-10T21:12:18.726+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :         Field(id,INT8,8,T)
2024-11-10T21:12:18.726+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :         Field(textfield,TEXT,65535,T)
2024-11-10T21:12:18.726+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :         Field(datefield,DATE,4,T)
2024-11-10T21:12:18.726+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:18.726+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:18.727+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE PortalSuspended
2024-11-10T21:12:18.731+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE ReadyForQuery(T)
2024-11-10T21:12:18.732+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Execute(portal=C_3,limit=2)
2024-11-10T21:12:18.732+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Sync
2024-11-10T21:12:18.734+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:18.734+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:18.734+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE PortalSuspended
2024-11-10T21:12:18.734+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE ReadyForQuery(T)
2024-11-10T21:12:18.734+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Execute(portal=C_3,limit=2)
2024-11-10T21:12:18.734+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Sync
2024-11-10T21:12:18.735+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:18.736+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:18.736+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE PortalSuspended
2024-11-10T21:12:18.736+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE ReadyForQuery(T)
```

## Логи когда курсор не срабатывает

Во-первых, мы не увидим текста о том, что открыта транзакция.

Можно обратить внимание на фрагмент:
```
Execute(portal=null,limit=0)
```
В отличие от корректного курсорного чтения, тут portal = null, а limit указан 0.
Также, такой вызов всего один.

А строк
```
DataRow(len=47)
```
после первого же execute столько, сколько строк в БД.

Важный момент, в логе в фрагменте:
```
simple execute, handler=org.postgresql.jdbc.PgStatement$StatementResultHandler@10b8b900, maxRows=0, fetchSize=2, flags=17
```

Указан fetchSize, однако этот параметр игнорируется, если нет транзакции.

Полный фрагмент таких логов:
```logs
2024-11-10T21:12:19.038+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :   simple execute, handler=org.postgresql.jdbc.PgStatement$StatementResultHandler@10b8b900, maxRows=0, fetchSize=2, flags=17
2024-11-10T21:12:19.039+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Parse(stmt=null,query="SELECT * FROM test_table",oids={})
2024-11-10T21:12:19.039+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Bind(stmt=null,portal=null)
2024-11-10T21:12:19.039+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Describe(portal=null)
2024-11-10T21:12:19.039+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Execute(portal=null,limit=0)
2024-11-10T21:12:19.039+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Sync
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE ParseComplete [null]
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE BindComplete [unnamed]
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE RowDescription(3)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :         Field(id,INT8,8,T)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :         Field(textfield,TEXT,65535,T)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :         Field(datefield,DATE,4,T)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=47)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=48)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE CommandStatus(SELECT 10)
2024-11-10T21:12:19.041+03:00 TRACE 32140 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE ReadyForQuery(I)
```