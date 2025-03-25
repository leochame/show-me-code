package com.chain_of_responsibility.handler.impl;

import com.chain_of_responsibility.handler.Handler;

public class ConcreteHandler1 extends Handler {
    @Override
    public void HandlerRequest(int request) {
        if (request >= 0 && request < 10) {
            System.out.println("ConcreteHandler1 处理请求 " + request);
        } else if (null != successor) {
            successor.HandlerRequest(request);
        }
    }
}

