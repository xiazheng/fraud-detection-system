**Description**:

The task is to create a Fraud Detection System in cloud. E.g. AWS.
Using messaging service SQS, cloud-native logging service Cloudwatch, cluster EKS, etc.


**How to build and deploy service**
1. Build FraudDetectionSystem-1.0-SNAPSHOT.jar
   ```bash
   cd  ~/IdeaProjects/FraudDetectionSystem
   mvn package -Dmaven.test.skip=true
   ```
2. Build docker imgage
   ```bash
   docker build -t fds-app .
   ```
3. Push to AWS ECR
   ```bash
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 061051229897.dkr.ecr.us-east-1.amazonaws.com
   docker tag fds-app:latest 061051229897.dkr.ecr.us-east-1.amazonaws.com/fds-app:latest
   docker push 061051229897.dkr.ecr.us-east-1.amazonaws.com/fds-app:latest
   ```
4. Create EKS cluster
   ```bash
   eksctl create cluster --name fds-eks2  --region us-east-1 --nodegroup-name standard-workers --node-type t2.medium --nodes 2 --nodes-min 1 --nodes-max 4 --managed --version 1.31
   ```
   Set cluster **Autoscaling** by adding AmazonEKSBlockStoragePolicy, AmazonEKSComputePolicy, AmazonEKSLoadBalancingPolicy and AmazonEKSNetworkingPolicy to cluster IAM role. E.g. eksctl-fds-eks2-cluster-ServiceRole-t1g4DdDeeqeL.
   And switch on **EKS Auto Mode** in EKS web portal.
   
5. Add AmazonSQSFullAccess policy for EKS NodeInstance role of the fds-eks2 cluster in aws web portal.

6. Create queue fds-in-queue, fds-out-queue and fds-dl-queue (dead letter queue for fds-in-queue)
   Either in SQS web portal or using java class org.zheng.fds.util.SQSQueueSetup.
   
7. Update kubeconfig file
   ```bash
   aws eks --region us-east-1 update-kubeconfig --name fds-eks2
   ```
8. Deploy the fds docker image to EKS cluster
   ```bash
   kubectl apply -f deployment.yaml
   ```
9. Enable **Cloudwatch** logging for cluster fds-eks2 for all types, api, audit, authenticator, controllerManager and scheduler.
   ```bash   
   eksctl utils update-cluster-logging --enable-types=all --region=us-east-1 --cluster=fds-eks2
   eksctl utils update-cluster-logging --enable-types=all --region=us-east-1 --cluster=fds-eks2 --approve
   ```
   Cluster logging is available at /aws/eks/fds-eks2/cluster
10. Verify whether the node and pod are running fine
    ```bash
    kubectl get nodes
    kubectl get pod
    ```
    
**JUnit Test**
1) org.zheng.fds.MessageProcessorTest to check whether the fraud score is assigned as expected.
2) org.zheng.fds.Queue: simple test of SQS Localstack (running in local docker image)
3) org.zheng.fds.IntegrationTest to check whether the queue is created and message is correctly processed.
   SQS Localstack (running in local docker image)

**Integration Test**
1. Use run java class org.zheng.fds.util.SQSMessageProducer to write test message into fds-in-queue
2. Check the log information in each of the pod.
   ```bash
   kubectl get pod  
   kubectl logs <pod-name>
   ```

**Resilience Test**
1. Delete queues using org.zheng.fds.SQSQueueCleanup class
2. Create queues using org.zheng.fds.SQSQueueSetup class
3. Create queues using org.zheng.fds.SQSMessageProducer class
4. Check pod log
   ```bash
   kubectl logs <pod-name>
   ```

**Note:** Fraud transaction is simulated by using SQSMessageProducer to put message into fds-in-queue
The risk score information is written into console and log file.