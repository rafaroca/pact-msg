package de.codecentric.pact

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClient
import org.testcontainers.containers.localstack.LocalStackContainer

object SQSHelper {
    fun setupSQSTestcontainer(localstack: LocalStackContainer): Pair<AmazonSQS, String> {
        System.setProperty("aws.accessKeyId", "testcontainer")
        System.setProperty("aws.secretKey", "testcontainer")
        System.setProperty("aws.region", "testcontainer")

        val sqsClient = AmazonSQSClient
            .builder()
            .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SQS))
            .withCredentials(localstack.defaultCredentialsProvider).build()

        val queueUrl = sqsClient.createQueue("testQueue").queueUrl
            .replace("localhost", localstack.containerIpAddress)

        return Pair(sqsClient, queueUrl)
    }
}
