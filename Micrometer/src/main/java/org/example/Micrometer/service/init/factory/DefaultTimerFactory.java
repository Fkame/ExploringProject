package org.example.Micrometer.service.init.factory;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.example.Micrometer.model.MonitoringMetric;

import java.util.List;

public class DefaultTimerFactory implements TimerFactory {

    private static final boolean NEED_HISTOGRAM = false;
    private static final double[] MONITORING_PERCENTILIES = { 0.5, 0.95 };

    @Override
    public Timer createAndRegister(MonitoringMetric metric, MeterRegistry meterRegistry) {
        return Timer.builder(metric.getName())
                .publishPercentileHistogram(NEED_HISTOGRAM)
                .publishPercentiles(MONITORING_PERCENTILIES)
                .register(meterRegistry);
    }
}
