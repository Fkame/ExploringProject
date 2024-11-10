package org.jdbc_cursor.dao;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.postgresql.core.v3.QueryExecutorImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>1. Обязательно добавляем в проперти выской уровень детализации логов: <pre>{@code
 *  logging:
 *   level:
 *     org.springframework.transaction: TRACE
 *     org.springframework.jdbc: TRACE
 *     org.postgresql.core.v3: TRACE
 * }</pre>
 *
 * <p>2. Условия открытия курсора: <pre>
 *     1. Установлен fetchSize в JdbcTemplate
 *     2. Выполнение запроса происходит в транзакции.
 * </pre>
 */
@SpringBootTest
class CursorReadingIntegrationTest {

    private static final Random RND = new Random();
    private static final String INSERT_TEMPLATE = """
            INSERT INTO test_table (id, textField, dateField) 
            VALUES (?, ?, ?);""";

    private static final String SELECT_TEMPLATE = """
            SELECT * FROM test_table""";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void cursorWorksFine() {
        clearTable();

        final int allRowsToRead = 10;
        final int fetchSizeOfBatchToRead = 2;

        jdbcTemplate.setFetchSize(fetchSizeOfBatchToRead);

        // Заполнение таблицы для чтения
        generateRowsInTable(allRowsToRead);

        // Получим логгер для сбора, установим минимальный уровень
        Logger logger = (Logger) LoggerFactory.getLogger(QueryExecutorImpl.class);
        logger.setLevel(Level.TRACE);
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logger.addAppender(logs);

        logs.start();

        // Выполнение логики
        readAll();

        logs.stop();

        // Нужно поискать сообщения об открытии портала, пример текста такого лога: Execute(portal=C_2,limit=2)
        // где limit=2 - размер установленного fetchSize - количества получаемых строк за 1 вызов fetch
        List<String> fetchFromPortalMessages = logs.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(log -> log.contains("Execute(portal="))
                .toList();

        assertFalse(fetchFromPortalMessages.isEmpty());

        // Проверим, что метод fetch вызывался несколько раз.
        // Вызовов fetch может быть больше наших расчетов, поэтому просто проверим, что их достаточно
        assertThat(allRowsToRead / fetchSizeOfBatchToRead).isLessThanOrEqualTo(fetchFromPortalMessages.size());

        // Проверим, что fetch брал нужное количество записей
        final String fetchLimitExpected = String.format("limit=%d", fetchSizeOfBatchToRead);
        assertTrue(fetchFromPortalMessages.stream()
                .allMatch(log -> log.contains(fetchLimitExpected))
        );
    }

    @Test
    void cursorDoesNotOpen() {
        // Поскольку Transactional не вешается, данные будут сохраняться в БД, нужно очищать их перед прогоном
        clearTable();

        final int allRowsToRead = 10;
        final int fetchSizeOfBatchToRead = 2;

        jdbcTemplate.setFetchSize(fetchSizeOfBatchToRead);

        generateRowsInTable(allRowsToRead);

        // Получим логгер для сбора, установим минимальный уровень
        Logger logger = (Logger) LoggerFactory.getLogger(QueryExecutorImpl.class);
        logger.setLevel(Level.TRACE);
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logger.addAppender(logs);

        logs.start();

        // Выполнение логики
        readAll();

        logs.stop();

        // В данном случае портал не открывается и fetchSize игнорируется
        List<String> fetchFromPortalMessages = logs.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(log -> log.contains("Execute(portal=null,limit=0)"))
                .toList();
        assertEquals(1, fetchFromPortalMessages.size());
    }

    private void readAll() {
        jdbcTemplate.query(SELECT_TEMPLATE, rs -> {
                    if (rs.isClosed()) {
                        throw new RuntimeException("ResultSet was closed!");
                    }

                    // Вызов этого метода каждый fetchSize раз должен вызывать .fetch и открытие портала постгреса для дотягивания значений
                    while (rs.next()) {

                    }
                    return null;
                }
        );
    }

    private void generateRowsInTable(int rowsAmount) {
        AtomicLong idGenerator = new AtomicLong(0);
        for (int i = 0; i < rowsAmount; i++) {
            jdbcTemplate.update(INSERT_TEMPLATE, ps -> {
                ps.setLong(1, idGenerator.incrementAndGet());
                ps.setString(2, randomStr());
                ps.setDate(3, randomDate());
            });
        }
    }

    private String randomStr() {
        return UUID.randomUUID().toString();
    }

    private Date randomDate() {
        return new Date(LocalDate.of(
                RND.nextInt(1800, 2500),
                RND.nextInt(1, 13),
                RND.nextInt(1, 28)
        ).toEpochDay());
    }

    private void clearTable() {
        jdbcTemplate.execute("TRUNCATE TABLE test_table");
    }

}