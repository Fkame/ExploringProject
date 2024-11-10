package org.jdbc_cursor.database.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "db.postgres")
@EnableConfigurationProperties
@Data
public class PostgresPropertiesProvider {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schema;
}
