package com.ntp.exception;

// 基础异常类
public class NtpException extends RuntimeException {
    public NtpException(String message, Throwable cause) { 
        super(message, cause); 
    }
}

