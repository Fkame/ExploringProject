package org.param_provider.provider;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ValueParameterProvider {

    @Value("${core.app_name}")
    private String appName;

    @Value("${core.appInfo.version}")
    private Integer version;

    @Value("${core.appInfo.multithreading}")
    private boolean multithreading;

    @Value("${core.appInfo.comments}")
    private String comments;

    // Value не умеет в доставание массивов из yml
    @Value("#{'${core.appInfo.flags_example}'.split(',')}")
    private List<String> flagsExample;

    @PostConstruct
    public void logInfo() {
        log.info("""
                    
                    === ValueParameterProvider ===
                    appName = {}
                    version = {}
                    multithreading = {}
                    comments = {}
                    flagsExample = {}
                    ==============================""",
                appName,
                version,
                multithreading,
                comments,
                String.join(", ", flagsExample)
        );
    }
}
