package com.adam.aliyun.time.model;

import com.adam.aliyun.time.config.ServerConfig;

import java.net.InetSocketAddress;

public class ServerState {
    public final InetSocketAddress address;
    public final ServerConfig config;
    public long requestTime;
    public boolean responsePending;
    
    public ServerState(InetSocketAddress address, ServerConfig config) {
        this.address = address;
        this.config = config;
        this.requestTime = 0;
        this.responsePending = false;
    }
}
