package com.chain_of_responsibility.handler.impl;

import com.chain_of_responsibility.handler.Handler;

public class ConcreteHandler2 extends Handler {
    @Override
    public void HandlerRequest(int request) {
        if (request >= 10 && request < 20) {
            System.out.println("ConcreteHandler2 处理请求 " + request);
        } else if (null != successor) {
            successor.HandlerRequest(request);
        }
    }
}
