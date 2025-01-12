package org.zheng.fds;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zheng.fds.wrapper.SqsWrapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.zheng.fds.util.SQSMessageProducer.SAMPLE_MESSAGE_LIST;
import static org.zheng.fds.util.SQSQueueSetup.*;

public class IntegrationTest {


    public SqsWrapper wrapper;

    @Before
    public void setup() throws Exception {
        wrapper = new SqsWrapper();
        wrapper.init();

        try {
            // Create the main queue
            String mainQueueUrl = wrapper.createQueue(IN_QUEUE_NAME);
            wrapper.createQueue(OUT_QUEUE_NAME);
            // Create the dead letter queue
            String dlQueueUrl = wrapper.createQueue(DL_QUEUE_NAME);
            // Set up the redrive policy
            wrapper.setDeadLetterQueueAttributes(mainQueueUrl, dlQueueUrl);

            for (String message : SAMPLE_MESSAGE_LIST) {
                wrapper.sendMessage(IN_QUEUE_NAME, message);
            }
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

    @Test
    public void process() throws Exception {
        SqsClient sqsClient = wrapper.getSqsClient();
        new SQSMessageConsumer(sqsClient).processOnce();

        //check out queue
        List<String> outMessages = wrapper.receiveMessage(OUT_QUEUE_NAME);
        for (String outMessage : outMessages) {
            System.out.println(outMessage);
        }
        assertEquals(4, outMessages.size());
    }

    @After
    public void close() {
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
