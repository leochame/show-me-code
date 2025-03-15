package com.aliyun.time.model;

import com.aliyun.time.config.ServerConfig;

public class NTPResponse {
    public final ServerConfig config;
    public final long offset;
    public final long rtt;
    
    public NTPResponse(ServerConfig config, long offset, long rtt) {
        this.config = config;
        this.offset = offset;
        this.rtt = rtt;
    }
}
