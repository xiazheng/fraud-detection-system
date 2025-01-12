Description:

The task is to create a Fraud Detection Ssystem on cloud. E.g. AWS.
Using messaging service SQS, cloud-native logging service Cloudwatch, cluster EKS, etc.


How to build and deploy  service
1. Build FraudDetectionSystem-1.0-SNAPSHOT.jar
   cd  /mnt/c/Users/Zheng/IdeaProjects/FraudDetectionSystem
   mvn package  -Dmaven.test.skip=true
2. Build docker imgage
   docker build -t fds-app .
3. Push to AWS ECR
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 061051229897.dkr.ecr.us-east-1.amazonaws.com
   docker tag fds-app:latest 061051229897.dkr.ecr.us-east-1.amazonaws.com/fds-app:latest
   docker push 061051229897.dkr.ecr.us-east-1.amazonaws.com/fds-app:latest
4. Create EKS cluster
   eksctl create cluster --name fds-eks2  --region us-east-1 --nodegroup-name standard-workers --node-type t2.medium --nodes 2 --nodes-min 1 --nodes-max 4 --managed --version 1.31
5. Add SQSAllAccess rights for EKS Node role of the fds-eks2 cluster in aws web portal.
6. Creat queue fds-in-queue, fds-out-queue and fds-dl-queue (dead letter queue for fds-in-queue)
   Either in SQS web portal or using java class org.zheng.fds.util.SQSQueueSetup.
   If the java class is used command "aws sts get-session-token" to get the temp credentials and update the correspondant values in class SqsServerWrapper for AwsSessionCredentials.create method.
7. Update kubeconfig file
   aws eks --region us-east-1 update-kubeconfig --name fds-eks2
8. Deploy the fds docker image to EKS cluster
   kubectl apply -f kubernetes.yaml
9. Enable Cloudwatch logging for cluster fds-eks2 for all types, api, audit, authenticator, controllerManager and scheduler.
   eksctl utils update-cluster-logging --enable-types=all --region=us-east-1 --cluster=fds-eks2
   eksctl utils update-cluster-logging --enable-types=all --region=us-east-1 --cluster=fds-eks2 --approve
10. Verify whether the node and pod are running fine
    kubectl get nodes
    kubectl get pod

JUnit Test
1) org.zheng.fds.MessageProcessorTest to check whether the fraud score is assigned as expected.
2) org.zheng.fds.Queue: simple test of SQS Localstack (running in local docker image)
3) org.zheng.fds.IntegrationTest to check whether the queue is created and message is correctly processed.
   SQS Localstack (running in local docker image)

Integration Test
1. Use run java class org.zheng.fds.util.SQSMessageProducer to write test message into fds-in-queue
2. Check the log information in each of the pod.
   kubectl get pod  
   kubectl logs <pod-name>

Note: Fraud transaction is simulated by using SQSMessageProducer to put message into fds-in-queue
The information is written into console and log file.

Resilience Test
1. Create Queue
2. write to queue
3. read from queue, process and write to output queue