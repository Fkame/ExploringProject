package org.example.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestTableDao {

    private final JdbcTemplate jdbcTemplate;

    public int insert(int id, String text) {
        return jdbcTemplate.update("INSERT INTO test_table(id, textField) VALUES (?, ?)",
                ps -> {
                    ps.setLong(1, id);
                    ps.setString(2, text);
                }
        );
    }
}
