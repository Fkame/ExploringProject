package org.example.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PostgresDataSourceConfiguration {

    private final PostgresPropertiesProvider postgresPropertiesProvider;

    @Bean("postgresqlDataSource")
    public DataSource getPostgresqlDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();

        dataSource.setUrl(postgresPropertiesProvider.getUrl());
        dataSource.setUser(postgresPropertiesProvider.getUsername());
        dataSource.setPassword(postgresPropertiesProvider.getPassword());

        log.info("""
                        === Postgres DataSource ===
                        url: {}
                        username: {}
                        password: {}
                        ============================""",
                postgresPropertiesProvider.getUrl(),
                postgresPropertiesProvider.getUsername(),
                postgresPropertiesProvider.getPassword()
        );

        return dataSource;
    }
}
