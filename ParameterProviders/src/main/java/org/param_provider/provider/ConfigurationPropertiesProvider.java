package org.param_provider.provider;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

/**
 * Пример использования ConfigurationProperties.
 *
 * По аннотациям:
 * 1. ConfigurationProperties: к имени префикса есть требования
 *  - отдельное значение должно быть в нижнем регистре с допустимым дефисом
 *  - Нельзя: ext.appInfo / ext.app_info
 *  - Можно: ext.app-info / ext.appinfo
 * 2. RequiredArgsConstructor: если поля final - нужен конструктор.
 * 3. PropertySource: загрузка пропертей из стороннего файла с конфигом.
 * 4. Configuration: класс должен быть бином.
 *
 * [WARN] Больше нельзя делать ConfigurationProperties с таким же префиксом - спринг не позволит.
 */
@Configuration
@PropertySource(value = "classpath:my-custom-props.yml", factory = org.param_provider.config.YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "ext.app-info")
@Getter
@Slf4j
@RequiredArgsConstructor
public class ConfigurationPropertiesProvider {

    private final Integer version;
    private final boolean multithreading;
    private final String comments;
    private final List<String> flagsExample;

    @PostConstruct
    public void logInfo() {
        log.info("""
                   
                    === ConfigurationPropertiesProvider ===
                    version = {}
                    multithreading = {}
                    comments = {}
                    flagsExample = {}
                    =======================================""",
                version,
                multithreading,
                comments,
                String.join(", ", flagsExample)

        );
    }
}
