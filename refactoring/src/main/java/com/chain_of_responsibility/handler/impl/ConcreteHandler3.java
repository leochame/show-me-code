package com.chain_of_responsibility.handler.impl;

import com.chain_of_responsibility.handler.Handler;

public class ConcreteHandler3 extends Handler {
    @Override
    public void HandlerRequest(int request) {
        if (request >= 20 && request < 30) {
            System.out.println("ConcreteHandler3 处理请求 " + request);
        } else if (null != successor) {
            successor.HandlerRequest(request);
        }
    }
}
