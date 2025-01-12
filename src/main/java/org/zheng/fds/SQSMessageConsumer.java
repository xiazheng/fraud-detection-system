package org.zheng.fds;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zheng.fds.util.SQSQueueSetup;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

public class SQSMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(SQSMessageConsumer.class);

    String queueUrl;
    String outQueueUrl;
    SqsClient sqsClient;

    public SQSMessageConsumer(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
        logger.info("Start of SQSMessageConsumer");
        queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(SQSQueueSetup.IN_QUEUE_NAME)).queueUrl();
        logger.info("In Queue URL: " + queueUrl);
        outQueueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(SQSQueueSetup.OUT_QUEUE_NAME)).queueUrl();
        logger.info("Out Queue URL: " + outQueueUrl);
    }

    public void process() {
        MessageProcessor processor = new MessageProcessor();
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .build();
        try {
            while (true) {
                sqsClient.receiveMessage(receiveMessageRequest).messages().forEach(message -> {
                    try {
                        // Process the message here
                        logger.info("Processing message: " + message.body());
                        String body = message.body();
                        Gson gson = new Gson();
                        Message messageObj = gson.fromJson(body, Message.class);
                        int score = processor.processMessage(messageObj);

                        messageObj.setRiskScore(score);
                        String outputMessage = gson.toJson(messageObj);

                        // Send the message to output queue
                        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                .queueUrl(outQueueUrl)
                                .messageBody(outputMessage)
                                .build();
                        sqsClient.sendMessage(sendMessageRequest);
                        if (score >= 100) {
                            logger.warn("Message " + outputMessage + " has a risk score of " + score);
                        }

                        // Delete the message after successful processing
                        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .receiptHandle(message.receiptHandle())
                                .build();
                        sqsClient.deleteMessage(deleteMessageRequest);
                    } catch (SqsException e) {
                        logger.error("Failed to delete message: " + e.awsErrorDetails().errorMessage(), e);
                        //System.err.println("Failed to delete message: " + e.awsErrorDetails().errorMessage());
                    } catch (Exception e) {
                        logger.error("Failed to delete message: " + e.getMessage(), e);
                    }
                });
            }
        } catch (SqsException e) {
            logger.error(e.awsErrorDetails().errorMessage());
        } finally {
            sqsClient.close();
        }
    }

    public void processOnce() {
        MessageProcessor processor = new MessageProcessor();
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl).maxNumberOfMessages(10)
                .build();
        try {
            {
                sqsClient.receiveMessage(receiveMessageRequest).messages().forEach(message -> {
                    try {
                        // Process the message here
                        logger.info("Processing message: " + message.body());
                        String body = message.body();
                        Gson gson = new Gson();
                        Message messageObj = gson.fromJson(body, Message.class);
                        int score = processor.processMessage(messageObj);

                        messageObj.setRiskScore(score);
                        String outputMessage = gson.toJson(messageObj);

                        // Send the message to output queue
                        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                .queueUrl(outQueueUrl)
                                .messageBody(outputMessage)
                                .build();
                        sqsClient.sendMessage(sendMessageRequest);
                        if (score >= 100) {
                            logger.warn("Message " + outputMessage + " has a risk score of " + score);
                        }

                        // Delete the message after successful processing
                        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .receiptHandle(message.receiptHandle())
                                .build();
                        sqsClient.deleteMessage(deleteMessageRequest);
                    } catch (SqsException e) {
                        logger.error("Failed to delete message: " + e.awsErrorDetails().errorMessage(), e);
                        //System.err.println("Failed to delete message: " + e.awsErrorDetails().errorMessage());
                    } catch (Exception e) {
                        logger.error("Failed to delete message: " + e.getMessage(), e);
                    }
                });
            }
        } catch (SqsException e) {
            logger.error(e.awsErrorDetails().errorMessage());
        }
    }

    public static void main(String[] args) {

        Region region = Region.US_EAST_1;
        while (true) {
            try {
                SqsClient sqsClient = SqsClient.builder()
                        .region(region)
                        .credentialsProvider(DefaultCredentialsProvider.create()).build();

                new SQSMessageConsumer(sqsClient).process();
            } catch (Exception e) {
                if (e instanceof QueueDoesNotExistException) {
                    logger.error("Queue does not exists: " + e.getMessage() + ". Wait for 1 seconds.");
                } else {
                    logger.error("Error while connection to SQS " + e.getMessage() + ". Wait for 1 seconds.");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}