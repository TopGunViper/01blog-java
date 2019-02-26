package edu.ouc.dist.retry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestCalculator {

    @Test
    public void test() {

        Calculator calculator = CalcClientProxy.getProxy();

        assertEquals(7, calculator.add(3, 4));
    }

}
