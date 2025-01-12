package org.zheng.fds.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingExample {
    private static final Logger logger = LoggerFactory.getLogger(LoggingExample.class);

    //StaticLoggerBinder a;
    public static void main(String[] args) {
        logger.info("This is a log message");
    }
}