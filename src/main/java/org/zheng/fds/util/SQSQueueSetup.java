package org.zheng.fds.util;

import org.zheng.fds.wrapper.AbstractSqsWrapper;
import org.zheng.fds.wrapper.SqsServerWrapper;
import software.amazon.awssdk.services.sqs.model.*;

public class SQSQueueSetup {

    public static final String IN_QUEUE_NAME = "fds-in-queue";
    public static final String OUT_QUEUE_NAME = "fds-out-queue";
    public static final String DL_QUEUE_NAME = "fds-dl-queue";  //dead letter queue

    public static void main(String[] args) throws Exception {

        AbstractSqsWrapper wrapper = getWrapper();
        wrapper.init();
        try {
            // Create the main queue and dead letter queue
            wrapper.createQueueWithDLQueue(IN_QUEUE_NAME, DL_QUEUE_NAME);
            // create output queue
            wrapper.createQueue(OUT_QUEUE_NAME);

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        } finally {
            wrapper.close();
        }
    }

    public static AbstractSqsWrapper getWrapper() {
        return new SqsServerWrapper();
    }
}
