package org.example.Micrometer.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.NamingConvention;
import org.example.Micrometer.model.InitializedMetricsDto;
import org.example.Micrometer.model.RequiredInitializationMetrics;
import org.example.Micrometer.model.MonitoringMetric;
import org.example.Micrometer.service.init.MetricsInitializationService;
import org.example.Micrometer.service.MonitoringService;
import org.example.Micrometer.service.init.factory.DefaultTimerFactory;
import org.example.Micrometer.service.init.factory.TimerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class MonitoringConfiguration {

    private static final List<MonitoringMetric> METRICS_BLACKLIST = List.of(
            MonitoringMetric.SOME_METRIC_TO_LOCK_BY_FILTER
    );

    @Bean
    public RequiredInitializationMetrics requiredInitializationMetrics() {
        return new RequiredInitializationMetrics(Set.of(
                MonitoringMetric.CACHE_SIZE_AMOUNT,
                MonitoringMetric.SCHEDULER_ERRORS
        ));
    }

    @Bean
    public MonitoringService monitoringService(MeterRegistry meterRegistry,
                                               RequiredInitializationMetrics requiredInitializationMetrics) {
        TimerFactory timerFactory = new DefaultTimerFactory();
        MetricsInitializationService metricsInitializationService = new MetricsInitializationService(timerFactory);

        InitializedMetricsDto initializedMetricsDto = metricsInitializationService.init(
                meterRegistry,
                requiredInitializationMetrics.getMetrics()
        );

        return new MonitoringService(meterRegistry, initializedMetricsDto.getGauges());
    }

    @Bean
    public MeterFilter customProgrammedMeterBlackListFilter() {

        Set<String> metricNamesBlakcList = METRICS_BLACKLIST.stream()
                .map(MonitoringMetric::getName)
                .collect(Collectors.toUnmodifiableSet());

        return MeterFilter.deny(
                id -> metricNamesBlakcList.contains(
                        id.getConventionName(NamingConvention.snakeCase).toLowerCase()
                )
        );
    }
}
