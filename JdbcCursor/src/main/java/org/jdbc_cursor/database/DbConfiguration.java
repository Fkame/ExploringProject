package org.example.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbConfiguration {

    private final DbGeneralPropertiesProvider dbGeneralPropertiesProvider;

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.setFetchSize(dbGeneralPropertiesProvider.getFetchSize());

        // Можно добавить хендлер ошибок ещё

        log.info("Created jdbcTemplate with fetch-size = {}", dbGeneralPropertiesProvider.getFetchSize());

        return jdbcTemplate;
    }
}
