package com.market.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

@Component
public class MetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(MetricsCollector.class);

    @Scheduled(fixedRate = 60000)
    public void collectJvmMetrics() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

        double heapUsedMB = heapUsage.getUsed() / (1024.0 * 1024.0);
        double heapMaxMB = heapUsage.getMax() / (1024.0 * 1024.0);
        double heapUsagePercent = (heapUsedMB / heapMaxMB) * 100;
        double cpuLoad = osMXBean.getSystemLoadAverage();

        log.info("JVM Metrics: heap={:.1f}MB/{:.1f}MB ({:.1f}%), cpuLoad={:.2f}",
                heapUsedMB, heapMaxMB, heapUsagePercent, cpuLoad);

        if (heapUsagePercent > 85) {
            log.warn("JVM heap usage HIGH: {:.1f}%", heapUsagePercent);
        }
    }
}
