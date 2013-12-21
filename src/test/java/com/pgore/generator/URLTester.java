package com.pgore.generator;

import org.junit.Test;

import java.io.IOException;

public class URLTester {
    @Test
    public void getUrl() throws IOException {
        System.out.println(PutUrlGenerator.generateUrl("pgore_dev_bucket","flags2.mp4",100000));
    }
}
