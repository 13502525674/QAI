package com.ape.apeadmin.domain;

import lombok.Data;

/**
 * @author system
 * @version 1.0
 * @description: 系统负载数据模型
 * @date 2026/03/31
 */
@Data
public class SystemLoadInfo {
    
    /** CPU 使用率 (%) */
    private Double cpuUsage;
    
    /** 内存使用率 (%) */
    private Double memoryUsage;
    
    /** 已用内存 (字节) */
    private Long memoryUsed;
    
    /** 最大可用内存 (字节) */
    private Long memoryMax;
    
    /** JVM 总内存 (字节) */
    private Long memoryTotal;
    
    /** JVM 空闲内存 (字节) */
    private Long memoryFree;
    
    /** CPU 核心数 */
    private Integer cpuCores;
    
    /** 系统平均负载 */
    private Double systemLoadAverage;
    
    /** 采集时间戳 */
    private Long timestamp;
    
    /** 操作系统名称 */
    private String osName;
    
    /** Java 版本 */
    private String javaVersion;
}
