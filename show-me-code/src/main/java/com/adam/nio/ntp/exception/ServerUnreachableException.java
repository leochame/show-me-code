package com.adam.nio.ntp.exception;

// 服务不可达异常（网页3服务器状态管理相关）
public class ServerUnreachableException extends NtpException {
    public ServerUnreachableException(String host) {
        super("NTP server unreachable: " + host, null);
    }
}
