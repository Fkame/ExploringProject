package org.example.Micrometer.service.init.factory;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.example.Micrometer.model.MonitoringMetric;

public interface TimerFactory {

    Timer createAndRegister(MonitoringMetric metric, MeterRegistry meterRegistry);
}
