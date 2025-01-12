package org.zheng.fds;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zheng.fds.wrapper.SqsWrapper;

import static org.junit.Assert.assertEquals;

public class QueueTest {
    public SqsWrapper sqsClient;

    String queueName="in-queue";

    @Before
    public void setup() throws Exception {
        sqsClient = new SqsWrapper();
        sqsClient.init();
        sqsClient.createQueue(queueName);
    }

    @Test
    public void test() throws Exception {
        sqsClient.sendMessage(queueName, "ASC");
        assertEquals(sqsClient.receiveMessage(queueName).get(0), "ASC");
    }

    @After
    public void close() {
        sqsClient.deleteSQSQueue(queueName);
        sqsClient.close();
    }
}
