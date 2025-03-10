package com.aliyun.time;

import com.aliyun.time.core.NTPSynchronizer;
import com.aliyun.time.config.ServerConfig;
import com.aliyun.time.core.TimeOffsetManager;
import com.aliyun.time.filter.KalmanFilter;
import com.aliyun.time.core.ServerRegistry;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class EnhancedNTPClient {
    // 配置常量
    private static final int NTP_PACKET_SIZE = 48;
    private static final int TIMEOUT = 3000; // 毫秒
    private static final long SYNC_INTERVAL = 60000; // 同步间隔，默认1分钟
    private static final int MAX_FAILURES = 3; // 最大连续失败次数，触发熔断
    private static final long BREAK_DURATION = 30000; // 熔断持续30秒
    
    // NTP服务器配置
    private static List<ServerConfig> createServerConfigs() {
        List<ServerConfig> servers = new ArrayList<>();
        // 第一层服务器 - 阿里云主NTP服务器
        servers.add(new ServerConfig("ntp.aliyun.com", 123, 1));
        servers.add(new ServerConfig("ntp1.aliyun.com", 123, 1));
        
        // 第二层服务器 - 备用服务器
        servers.add(new ServerConfig("ntp.ntsc.ac.cn", 123, 2));
        servers.add(new ServerConfig("ntp.tencent.com", 123, 2));
        
        // 第三层服务器 - 公共NTP服务
        servers.add(new ServerConfig("cn.pool.ntp.org", 123, 3));
        servers.add(new ServerConfig("time.windows.com", 123, 3));
        
        return servers;
    }
    
    // 组件
    private final Selector selector;
    private final ServerRegistry serverRegistry;
    private final KalmanFilter kalmanFilter;
    private final TimeOffsetManager timeOffsetManager;
    private final NTPSynchronizer ntpSynchronizer;
    private final ScheduledExecutorService scheduler;

    private static volatile EnhancedNTPClient instance;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    /**
     * 构造函数 - 初始化NTP客户端
     */
    private EnhancedNTPClient() throws IOException {
        // 初始化选择器
        this.selector = Selector.open();
        
        // 初始化服务器注册表
        this.serverRegistry = new ServerRegistry(createServerConfigs(), selector, MAX_FAILURES, BREAK_DURATION);
        serverRegistry.initialize();
        
        // 初始化卡尔曼滤波器
        this.kalmanFilter = new KalmanFilter(1e-6, 1e-2);
        
        // 初始化时间偏移管理器
        this.timeOffsetManager = new TimeOffsetManager(kalmanFilter, serverRegistry);
        
        // 初始化NTP同步器
        this.ntpSynchronizer = new NTPSynchronizer(selector, serverRegistry, timeOffsetManager, TIMEOUT, NTP_PACKET_SIZE);
        
        // 初始化调度器
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // 启动定时同步任务
        scheduler.scheduleAtFixedRate(this::synchronizeTime, 0, SYNC_INTERVAL, TimeUnit.MILLISECONDS);
    }
    /**
     * 全局初始化方法（程序启动时调用）
     */
    public static synchronized void initialize() throws IOException {
        if (initialized.get()) return;

        instance = new EnhancedNTPClient();
        initialized.set(true);
    }

    /**
     * 获取单例实例
     */
    public static EnhancedNTPClient getInstance() throws IOException {
        if (!initialized.get()) {
            initialize();
        }
        return instance;
    }

    /**
     * 对外提供的时间获取接口
     */
    public Date getCurrentTime() {
        // 如果尚未初始化完成，先执行同步
        if (!timeOffsetManager.isInitialized()) {
            try {
                synchronizeTime();
            } catch (Exception e) {
                System.err.println("初始同步失败: " + e.getMessage());
            }
        }
        
        return timeOffsetManager.getCurrentTime();
    }
    
    /**
     * 时间同步过程
     */
    private void synchronizeTime() {
        ntpSynchronizer.synchronizeTime();
    }
    
    /**
     * 关闭资源
     */
    public void close() throws IOException {
        // 关闭调度器
        scheduler.shutdown();
        
        // 关闭所有通道
        serverRegistry.closeAll();
        
        // 关闭选择器
        selector.close();
        
        System.out.println("NTP客户端已关闭");
    }
    /**
     * 静态关闭方法
     */
    public static void shutdown() throws IOException {
        if (instance != null) {
            instance.close();
        }
    }

    /**
     * 确保JVM退出时关闭资源
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (IOException e) {
                System.err.println("关闭NTP客户端时出错: " + e.getMessage());
            }
        }));
    }

}