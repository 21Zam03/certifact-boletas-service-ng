package com.certicom.certifact_boletas_service_ng.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqsConfig {

    @Value("${apifact.aws.iam.access_key_id}")
    private String accessKey;

    @Value("${apifact.aws.iam.secret_access_key}")
    private String secretKey;

    @Value("${apifact.aws.s3.region}")
    private String region;

    @Bean
    public AmazonSQS amazonSQS() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonSQSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

}
