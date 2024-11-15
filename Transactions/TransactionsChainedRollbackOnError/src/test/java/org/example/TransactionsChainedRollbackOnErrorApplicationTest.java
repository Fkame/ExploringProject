package org.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.ServiceA;
import org.example.service.ServiceC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Slf4j
class TransactionsChainedRollbackOnErrorApplicationTest {

    @Autowired
    private ServiceA sut;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void clearBeforeAndAfter() {
        clearTable();
    }

    @Test
    void testTransactionChainedRollback() {
        TestTableEntity expected = TestTableEntity.builder()
                .id(ServiceC.SERVICE_ID)
                .textField(ServiceC.serviceText)
                .build();

        Exception ex = assertThrows(UnexpectedRollbackException.class, () -> sut.doLogic());
        log.info("Catched UnexpectedRollbackException as expected: ", ex);

        List<TestTableEntity> entities = new ArrayList<>(1);
        jdbcTemplate.query("SELECT * FROM test_table", rs -> {
            entities.add(TestTableEntity.builder()
                    .id(rs.getInt(1))
                    .textField(rs.getString(2))
                    .build());
        });

        assertEquals(1, entities.size());
        assertEquals(expected, entities.get(0));
    }

    private void clearTable() {
        jdbcTemplate.execute("TRUNCATE table test_table;");
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestTableEntity {
        private int id;
        private String textField;
    }
}
