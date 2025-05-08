package org.example.Micrometer.model;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Builder
@AllArgsConstructor
@Getter
public class InitializedMetricsDto {
    private final Map<MonitoringMetric, Counter> counters;
    private final Map<MonitoringMetric, AtomicLong> gauges;
    private final Map<MonitoringMetric, Timer> timers;
}
