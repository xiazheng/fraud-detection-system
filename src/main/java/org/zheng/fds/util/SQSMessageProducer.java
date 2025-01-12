package org.zheng.fds.util;

import org.zheng.fds.wrapper.AbstractSqsWrapper;

import java.util.Arrays;
import java.util.List;

import static org.zheng.fds.util.SQSQueueSetup.IN_QUEUE_NAME;
import static org.zheng.fds.util.SQSQueueSetup.getWrapper;

public class SQSMessageProducer {

    //fraud account id
    //fine
    //amount=100 fine
    //amount>100  fraud

   public static List<String> SAMPLE_MESSAGE_LIST = Arrays.asList(new String[]{
            "{\"transactionId\":\"12345\",\"accountId\":\"1234\",\"amount\":10.0,\"merchant\":\"ABC Store\"}",
            "{\"transactionId\":\"12346\",\"accountId\":\"12345\",\"amount\":10.0,\"merchant\":\"ABC Store\"}",
            "{\"transactionId\":\"12347\",\"accountId\":\"12345\",\"amount\":100.0,\"merchant\":\"ABC Store\"}",
            "{\"transactionId\":\"12348\",\"accountId\":\"12345\",\"amount\":150.0,\"merchant\":\"ABC Store\"}"
    });

    public static void main(String[] args) throws Exception {
        AbstractSqsWrapper wrapper = getWrapper();
        wrapper.init();

        for (String message:SAMPLE_MESSAGE_LIST)
        {
            wrapper.sendMessage(IN_QUEUE_NAME,message);
        }
        wrapper.close();
    }
}