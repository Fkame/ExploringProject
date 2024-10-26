package org.example.database;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class DbGeneralPropertiesProvider {

    @Value("${db.fetchSize}")
    private Integer fetchSize;
}
