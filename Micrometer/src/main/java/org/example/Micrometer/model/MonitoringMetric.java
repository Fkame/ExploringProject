package org.example.Micrometer.model;

import io.micrometer.core.instrument.Meter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MonitoringMetric {

    SOME_METRIC_TO_LOCK_BY_FILTER("my_metric_to_lock_by_filter", Meter.Type.COUNTER),
    SCHEDULER_ERRORS("scheduler_errors", Meter.Type.COUNTER),
    SCHEDULER_TIME("scheduler_time", Meter.Type.TIMER),
    CACHE_SIZE_AMOUNT("cache_size_amount", Meter.Type.GAUGE);

    private final String name;
    private final Meter.Type type;

}
