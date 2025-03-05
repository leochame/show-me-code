package com.adam.ntp.exception;

// 协议层异常（网页4的协议校验相关）
public class StratumViolationException extends NtpException {
    public StratumViolationException(int stratum) {
        super("Invalid stratum level: " + stratum, null);
    }
}
