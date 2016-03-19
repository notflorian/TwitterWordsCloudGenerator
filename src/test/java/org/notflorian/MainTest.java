package org.notflorian;

import org.junit.Test;

import static org.junit.Assert.*;


public class MainTest {

    @Test
    public void readFile() throws Exception {
        assertTrue(!Main.readFile("englishST.txt").isEmpty());
    }
}