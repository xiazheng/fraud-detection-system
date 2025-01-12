package org.zheng.fds.util;

import org.zheng.fds.wrapper.AbstractSqsWrapper;
import software.amazon.awssdk.services.sqs.model.SqsException;

import static org.zheng.fds.util.SQSQueueSetup.*;

public class SQSQueueCleanup {

    public static void main(String[] args) throws Exception {

        AbstractSqsWrapper wrapper =getWrapper();
        wrapper.init();
        try {
            // Delete queue
            wrapper.deleteSQSQueue(IN_QUEUE_NAME);
            wrapper.deleteSQSQueue(OUT_QUEUE_NAME);
            wrapper.deleteSQSQueue(DL_QUEUE_NAME);
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        } finally {
            wrapper.close();
        }
    }
}
