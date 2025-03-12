package com.adam.aliyun.time.config;

public class ServerConfig {
    public final String host;
    public final int port;
    public final int tier; // 服务器层级
    
    public ServerConfig(String host, int port, int tier) {
        this.host = host;
        this.port = port;
        this.tier = tier;
    }
}