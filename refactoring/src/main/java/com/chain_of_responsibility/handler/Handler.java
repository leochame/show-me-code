package com.chain_of_responsibility.handler;

public abstract class Handler {
    protected Handler successor;
    public void SetSuccesssor(Handler successor) {
        this.successor = successor;
    }

    public abstract void HandlerRequest(int request);
}
