package com.ericsson.esc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloModule {

    private static final Logger log = LoggerFactory.getLogger(HelloModule.class);

    public static void main(String[] args){
        System.out.print("Hello Module!");
        log.info("Message printed.");
    }
}
