package org.zheng.fds.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSqsWrapper {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSqsWrapper.class);
    protected SqsClient sqsClient;

    public abstract SqsClient buildSQSClient();

    public void sendMessage(String queueName, String message) {
        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();

        //String transactionData = "{\"transactionId\":\"12345\",\"amount\":100.0,\"merchant\":\"ABC Store\"}";
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
        try {
            sqsClient.sendMessage(sendMessageRequest);
            System.out.println("Transaction sent to SQS.");
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

    public List<String> receiveMessage(String queueName) {

        String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
        System.out.println("Queue URL: " + queueUrl);

        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .build();

        List<String> result = new ArrayList<>();
        try {
            sqsClient.receiveMessage(receiveMessageRequest).messages().forEach(message -> {
                try {
                    // Process the message here
                    String messageString = message.body();
                    result.add(messageString);
                    System.out.println("Processing message: " + message.body());
                    // Delete the message after successful processing
                    DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build();
                    sqsClient.deleteMessage(deleteMessageRequest);

                } catch (SqsException e) {
                    System.err.println("Failed to delete message: " + e.awsErrorDetails().errorMessage());
                }
            });
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return result;
    }

    public void close() {

        sqsClient.close();
    }

    public String createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queueName)
                .build();
        CreateQueueResponse createQueueResponse = sqsClient.createQueue(createQueueRequest);
        System.out.println(createQueueResponse.queueUrl());
        return createQueueResponse.queueUrl();
    }

    public void deleteSQSQueue(String queueName) {
        try {
            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();

            String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
            DeleteQueueRequest deleteQueueRequest = DeleteQueueRequest.builder()
                    .queueUrl(queueUrl)
                    .build();

            sqsClient.deleteQueue(deleteQueueRequest);
            logger.info("Queue Deleted " + queueName);

        } catch (SqsException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public String createQueueWithDLQueue(String queueName, String deadLetterQueue) {

        String queueUrl=this.createQueue(queueName);
        String dlQueueUrl=this.createQueue(deadLetterQueue);
        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queueName)
                .build();
        CreateQueueResponse createQueueResponse = sqsClient.createQueue(createQueueRequest);
        logger.info("Created Queue: "+createQueueResponse.queueUrl());

        // 构建获取队列属性的请求
        GetQueueAttributesRequest getQueueAttributesRequest = GetQueueAttributesRequest.builder()
                .queueUrl(dlQueueUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build();

        // 获取队列属性的响应
        GetQueueAttributesResponse response = sqsClient.getQueueAttributes(getQueueAttributesRequest);
        // 获取属性映射
        Map<QueueAttributeName, String> attributes = response.attributes();
        // 从映射中获取队列的 ARN
        String queueArn = attributes.get(QueueAttributeName.QUEUE_ARN);
        System.out.println("Queue ARN: " + queueArn);

        Map<QueueAttributeName, String> attributes2 = new HashMap<>();
        attributes2.put(QueueAttributeName.REDRIVE_POLICY, "{\"maxReceiveCount\":3,\"deadLetterTargetArn\":\"" + queueArn + "\"}");
        SetQueueAttributesRequest setQueueAttributesRequest = SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(attributes2)
                .build();
        sqsClient.setQueueAttributes(setQueueAttributesRequest);

        return createQueueResponse.queueUrl();
    }

    public void setDeadLetterQueueAttributes(String mainQueueUrl, String dlQueueUrl) {

        Map<QueueAttributeName, String> attributes = new HashMap<>();
        attributes.put(QueueAttributeName.REDRIVE_POLICY, "{\"maxReceiveCount\":3,\"deadLetterTargetArn\":\"" + dlQueueUrl + "\"}");
        SetQueueAttributesRequest setQueueAttributesRequest = SetQueueAttributesRequest.builder()
                .queueUrl(mainQueueUrl)
                .attributes(attributes)
                .build();
        sqsClient.setQueueAttributes(setQueueAttributesRequest);
    }

    public void init() throws Exception {
        this.sqsClient = this.buildSQSClient();
    }
}
