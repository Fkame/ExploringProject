package org.jdbc_cursor.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbc_cursor.database.provider.PostgresPropertiesProvider;
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
        dataSource.setCurrentSchema(postgresPropertiesProvider.getSchema());

        log.info("""
                        === Postgres DataSource ===
                        url: {}
                        username: {}
                        password: {}
                        schema: {}
                        ============================""",
                postgresPropertiesProvider.getUrl(),
                postgresPropertiesProvider.getUsername(),
                postgresPropertiesProvider.getPassword(),
                postgresPropertiesProvider.getSchema()
        );

        return dataSource;
    }
}
