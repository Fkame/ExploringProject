package org.example.Micrometer.service.init;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.example.Micrometer.model.InitializedMetricsDto;
import org.example.Micrometer.model.MonitoringMetric;
import org.example.Micrometer.service.init.factory.TimerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class MetricsInitializationService {

    private final TimerFactory timerFactory;

    public InitializedMetricsDto init(MeterRegistry meterRegistry,
                                      Set<MonitoringMetric> metricsToInitialize) {

        final Map<MonitoringMetric, Counter> counters = new HashMap<>();
        final Map<MonitoringMetric, AtomicLong> gauges = new HashMap<>();
        final Map<MonitoringMetric, Timer> timers = new HashMap<>();

        for (MonitoringMetric metric : metricsToInitialize) {
            switch (metric.getType()) {
                case COUNTER:
                    counters.put(metric, meterRegistry.counter(metric.getName()));
                    break;
                case TIMER:
                    timers.put(metric, timerFactory.createAndRegister(metric, meterRegistry));
                    break;
                case GAUGE:
                    gauges.put(metric, meterRegistry.gauge(metric.getName(), new AtomicLong(0)));
                    break;
                default:
                    throw new RuntimeException("Метрика = [%s] с типом = [%s] не поддерживается! Нужно добавить её инициализации");
            }
        }

        return InitializedMetricsDto.builder()
                .counters(counters)
                .gauges(gauges)
                .timers(timers)
                .build();
    }
}
