package org.example.Micrometer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class RequiredInitializationMetrics {
    private Set<MonitoringMetric> metrics;
}
