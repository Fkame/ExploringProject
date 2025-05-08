package org.example.Micrometer.service;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.example.Micrometer.model.MonitoringMetric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MonitoringService {

    private final MeterRegistry meterRegistry;
    private final Map<MonitoringMetric, AtomicLong> gauges;

    public MonitoringService(MeterRegistry meterRegistry, Map<MonitoringMetric, AtomicLong> gauges) {
        this.gauges = new HashMap<>(gauges);
        this.meterRegistry = meterRegistry;
    }

    public void notifyCounter(MonitoringMetric metric) {
        notifyCounter(metric, 1);
    }

    public void notifyCounter(MonitoringMetric metric, int amount) {
        if (!validateCounter(metric)) {
            return;
        }

        meterRegistry.counter(metric.getName())
                .increment(amount);
    }

    private boolean validateCounter(MonitoringMetric metric) {
        if (metric.getType() != Meter.Type.COUNTER) {
            log.warn("Попытка записать метрику [{}] как counter, хотя она типа [{}], изменение метрики будет подавлено",
                    metric.getName(),
                    metric.getType()
            );

            return false;
        }

        return true;
    }
}
