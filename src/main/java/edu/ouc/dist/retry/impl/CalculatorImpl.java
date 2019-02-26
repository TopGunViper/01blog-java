package edu.ouc.dist.retry.impl;

import edu.ouc.dist.retry.Calculator;

public class CalculatorImpl implements Calculator {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
