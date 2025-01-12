package org.zheng.fds.wrapper;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsServerWrapper extends AbstractSqsWrapper {

    public void init() {
        sqsClient = buildSQSClient();
    }

    public SqsClient buildSQSClient() {
        Region region = Region.US_EAST_1;
        SqsClient sqsClient = SqsClient.builder()
                .region(region)
         .credentialsProvider(DefaultCredentialsProvider.create()).build();

        return sqsClient;
    }
}
