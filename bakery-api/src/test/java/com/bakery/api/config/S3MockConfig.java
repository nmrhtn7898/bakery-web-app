package com.bakery.api.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

@TestConfiguration
public class S3MockConfig {

    private final S3Mock s3Mock;

    private final int port;

    public S3MockConfig() {
        this.port = findAvailableTcpPort();
        this.s3Mock = new S3Mock
                .Builder()
                .withPort(port)
                .withInMemoryBackend()
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        s3Mock.start();
    }

    @PreDestroy
    public void preDestroy() {
        s3Mock.stop();
    }

    @Bean
    public S3Mock s3Mock() {
        return s3Mock;
    }

    @Bean
    public AmazonS3 amazonS3(S3Mock s3Mock, @Value("${aws.s3.region}") String region,
                             @Value("${aws.s3.bucket}") String bucket, @Value("${aws.s3.mock.host}") String host) {
        EndpointConfiguration endpointConfiguration = new EndpointConfiguration(host + ":" + port, region);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new AnonymousAWSCredentials());
        AmazonS3 amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(credentialsProvider)
                .build();
        amazonS3.createBucket(bucket);
        return amazonS3;
    }

}
