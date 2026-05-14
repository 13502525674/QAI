package com.ape.apeadmin.controller.monitor;

import com.ape.apeadmin.domain.SystemLoadInfo;
import com.ape.apecommon.domain.Result;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author system
 * @version 1.0
 * @description: 系统监控控制器
 * @date 2026/03/31
 */
@Slf4j
@Controller
@ResponseBody
@RequestMapping("/monitor")
public class SystemMonitorController {

    /**
     * 获取系统负载信息
     */
    @GetMapping("/load")
    public Result getSystemLoad() {
        long startTime = System.currentTimeMillis();
        log.info("========== 开始采集系统负载数据 ==========");
        
        try {
            SystemLoadInfo info = new SystemLoadInfo();
            
            // ========== 使用 OSHI 库获取系统信息 ==========
            log.debug("初始化 OSHI SystemInfo...");
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hardware = systemInfo.getHardware();
            OperatingSystem os = systemInfo.getOperatingSystem();
            
            // ========== CPU 相关数据 ==========
            log.debug("采集 CPU 数据...");
            CentralProcessor processor = hardware.getProcessor();
            int cpuCores = processor.getLogicalProcessorCount();
            log.info("CPU 核心数: {}", cpuCores);
            
            // 获取 CPU 使用率（需要两次采样）
            log.debug("开始第一次 CPU 采样...");
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            log.debug("等待 500ms 进行第二次采样...");
            TimeUnit.MILLISECONDS.sleep(500);  // 等待500毫秒
            double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
            log.info("CPU 使用率: {}%", Math.round(cpuLoad * 100.0) / 100.0);
            
            // 系统负载（1分钟平均）
            double systemLoadAverage = processor.getSystemLoadAverage(1)[0];
            if (systemLoadAverage < 0) {
                systemLoadAverage = 0.0;
            }
            log.info("系统平均负载: {}", Math.round(systemLoadAverage * 100.0) / 100.0);
            
            info.setCpuCores(cpuCores);
            info.setCpuUsage(Math.round(cpuLoad * 100.0) / 100.0);
            info.setSystemLoadAverage(Math.round(systemLoadAverage * 100.0) / 100.0);
            
            // ========== 内存相关数据 ==========
            log.debug("采集内存数据...");
            GlobalMemory memory = hardware.getMemory();
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;
            
            log.info("总内存: {} GB", String.format("%.2f", totalMemory / 1024.0 / 1024 / 1024));
            log.info("可用内存: {} GB", String.format("%.2f", availableMemory / 1024.0 / 1024 / 1024));
            log.info("已用内存: {} GB", String.format("%.2f", usedMemory / 1024.0 / 1024 / 1024));
            log.info("内存使用率: {}%", Math.round(memoryUsage * 100.0) / 100.0);
            
            info.setMemoryMax(totalMemory);
            info.setMemoryUsed(usedMemory);
            info.setMemoryUsage(Math.round(memoryUsage * 100.0) / 100.0);
            
            // ========== JVM 内存信息 ==========
            Runtime runtime = Runtime.getRuntime();
            info.setMemoryTotal(runtime.totalMemory());
            info.setMemoryFree(runtime.freeMemory());
            log.debug("JVM 总内存: {} MB", runtime.totalMemory() / 1024 / 1024);
            log.debug("JVM 空闲内存: {} MB", runtime.freeMemory() / 1024 / 1024);
            
            // ========== 系统信息 ==========
            info.setOsName(os.toString());
            info.setJavaVersion(System.getProperty("java.version"));
            info.setTimestamp(System.currentTimeMillis());
            
            log.info("操作系统: {}", os.toString());
            log.info("Java 版本: {}", System.getProperty("java.version"));
            
            long endTime = System.currentTimeMillis();
            log.info("========== 数据采集完成，耗时: {} ms ==========", (endTime - startTime));
            
            return Result.success(info);
            
        } catch (Exception e) {
            log.error("获取系统负载失败", e);
            return Result.fail("获取系统负载失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取详细监控数据（包含磁盘、线程等）
     */
    @GetMapping("/detailed")
    public Result getDetailedMonitor() {
        try {
            Map<String, Object> data = new HashMap<>();
            
            // ========== JVM 信息 ==========
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> jvmInfo = new HashMap<>();
            jvmInfo.put("maxMemory", formatBytes(runtime.maxMemory()));
            jvmInfo.put("totalMemory", formatBytes(runtime.totalMemory()));
            jvmInfo.put("freeMemory", formatBytes(runtime.freeMemory()));
            jvmInfo.put("usedMemory", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
            jvmInfo.put("availableProcessors", runtime.availableProcessors());
            data.put("jvm", jvmInfo);
            
            // ========== 线程信息 ==========
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            Map<String, Object> threadInfo = new HashMap<>();
            threadInfo.put("count", threadBean.getThreadCount());
            threadInfo.put("peak", threadBean.getPeakThreadCount());
            threadInfo.put("daemon", threadBean.getDaemonThreadCount());
            threadInfo.put("totalStarted", threadBean.getTotalStartedThreadCount());
            data.put("threads", threadInfo);
            
            // ========== 系统信息 ==========
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("osVersion", System.getProperty("os.version"));
            systemInfo.put("osArch", System.getProperty("os.arch"));
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("javaHome", System.getProperty("java.home"));
            systemInfo.put("userName", System.getProperty("user.name"));
            
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            systemInfo.put("cpuCores", osBean.getAvailableProcessors());
            systemInfo.put("systemLoad", osBean.getSystemLoadAverage());
            data.put("system", systemInfo);
            
            // ========== 运行时信息 ==========
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            Map<String, Object> runtimeInfo = new HashMap<>();
            runtimeInfo.put("startTime", runtimeBean.getStartTime());
            runtimeInfo.put("uptime", runtimeBean.getUptime());
            runtimeInfo.put("vmName", runtimeBean.getVmName());
            runtimeInfo.put("vmVersion", runtimeBean.getVmVersion());
            data.put("runtime", runtimeInfo);
            
            return Result.success(data);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取详细监控数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 字节格式化工具
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }
}
