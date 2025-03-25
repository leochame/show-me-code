package com.strategy;

import com.strategy.context.Context;
import com.strategy.example.strategies.OperationAdd;
import com.strategy.example.strategies.OperationMultiply;
import com.strategy.example.strategies.OperationSubtract;

public class StrategyPatternDemo {
   public static void main(String[] args) {
      Context context = new Context(new OperationAdd());
      System.out.println("10 + 5 = " + context.executeStrategy(10, 5));
 
      context = new Context(new OperationSubtract());
      System.out.println("10 - 5 = " + context.executeStrategy(10, 5));
 
      context = new Context(new OperationMultiply());
      System.out.println("10 * 5 = " + context.executeStrategy(10, 5));
   }
}