package com.bread.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTest {

    @Test
    public void test() {
        com.bread.common.Test test = new com.bread.common.Test();
        assertEquals(test.a, 5);
    }


}
