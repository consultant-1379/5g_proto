package com.ericsson.esc2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloModule2 {

    private static final Logger log = LoggerFactory.getLogger(HelloModule2.class);

    public static void main(String[] args){
        System.out.print("Hello Module!");
        log.info("Message printed.");
    }
}
