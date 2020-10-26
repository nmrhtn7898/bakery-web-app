package com.bread.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommonTest {

    @Test
    public void test() {
        Common common = new Common();
        assertNotNull(common);
        assertEquals(common.a, 5);
    }

}
