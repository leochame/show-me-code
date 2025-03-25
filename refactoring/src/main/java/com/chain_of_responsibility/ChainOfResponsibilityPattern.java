package com.chain_of_responsibility;

import com.chain_of_responsibility.handler.impl.ConcreteHandler1;
import com.chain_of_responsibility.handler.impl.ConcreteHandler2;
import com.chain_of_responsibility.handler.impl.ConcreteHandler3;
import com.chain_of_responsibility.handler.Handler;

public class ChainOfResponsibilityPattern {
    public static void main(String[] args) {
        Handler h1 = new ConcreteHandler1();
        Handler h2 = new ConcreteHandler2();
        Handler h3 = new ConcreteHandler3();
        h1.SetSuccesssor(h2);
        h2.SetSuccesssor(h3);

        int[] requests = {2, 29, 9, 15, 4, 19};
        for (int i : requests) {
            h1.HandlerRequest(i);
        }
    }
}
