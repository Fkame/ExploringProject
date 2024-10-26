package org.param_provider.provider;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EnvParameterProvider {

    private final String appName;
    private final Integer version;
    private final Boolean multithreading;
    private final String comments;
    private final List<String> flagsExample;

    public EnvParameterProvider(Environment env) {
        this.appName = env.getProperty("core.app_name");
        this.version = env.getProperty("core.appInfo.version", Integer.class);
        this.multithreading = env.getProperty("core.appInfo.multithreading", Boolean.class);
        this.comments = env.getProperty("core.appInfo.comments");
        this.flagsExample = List.of(env.getProperty("core.appInfo.flags_example").split(","));
    }

    @PostConstruct
    public void logInfo() {
        log.info("""
                                            
                        === EnvParameterProvider ===
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
