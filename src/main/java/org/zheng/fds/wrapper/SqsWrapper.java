package org.zheng.fds.wrapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsWrapper extends AbstractSqsWrapper {

    // LocalStack endpoint for SQS
    static String endpoint = "http://localhost:4566";

    public SqsClient buildSQSClient() {
        // Dummy credentials
        SqsClient sqsClient = SqsClient.builder()

                .region(Region.US_EAST_1)
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build();
        return sqsClient;
    }

    public SqsClient getSqsClient() {
        return this.sqsClient;
    }

}
