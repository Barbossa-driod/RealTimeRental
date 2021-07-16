package com.safely.batch.connector.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.safely.batch.connector.terraform.TerraformOutput;
import com.safely.batch.connector.terraform.TerraformWorkspace;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.paramstore.AwsParamStoreProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties({AwsParamStoreProperties.class})
public class AwsConfiguration {

    private final String profileName = "safely";

    @Value("${IS_LOCAL:false}")
    private boolean isLocal;

    public AWSCredentialsProvider loadCredentials(List<TerraformOutput> terraformOutputs) {
        AWSCredentialsProvider credentialsProvider;
        if (isLocal) {
            String region = terraformOutputs.stream()
                    .filter(x -> x.getKey().equals("region"))
                    .findFirst()
                    .get().getStringValue();

            String roleArn = terraformOutputs.stream()
                    .filter(x -> x.getKey().equals("ecs_task_role_arn"))
                    .findFirst()
                    .get().getStringValue();

            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceAsyncClientBuilder.standard()
                    .withCredentials(new ProfileCredentialsProvider(profileName))
                    .withRegion(region)
                    .build();

            String sessionName = String.format("local-dev-%s", LocalDateTime.now().toString())
                    .replace(':', '.').replace(' ', '-');
            AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
                    .withDurationSeconds((int) Duration.ofHours(4).getSeconds())
                    .withRoleArn(roleArn)
                    .withRoleSessionName(sessionName);

            AssumeRoleResult assumeRoleResult = stsClient.assumeRole(assumeRoleRequest);
            Credentials credentials = assumeRoleResult.getCredentials();

            credentialsProvider = new AWSStaticCredentialsProvider(
                    new BasicSessionCredentials(credentials.getAccessKeyId(),
                            credentials.getSecretAccessKey(),
                            credentials.getSessionToken()));
        } else {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        }
        return credentialsProvider;
    }

    @Bean
    @Primary
    public AmazonSQSAsync sqsClient(TerraformWorkspace terraformWorkspace) throws Exception {
        List<TerraformOutput> terraformOutputs = null;
        if (isLocal) {
            terraformOutputs = terraformWorkspace.getOutputs();
        }
        AWSCredentialsProvider credentialsProvider = loadCredentials(terraformOutputs);

        AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(credentialsProvider);

        return builder.build();
    }

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory() {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setMaxNumberOfMessages(1);
        factory.setWaitTimeOut(20); // This could be configured if we wanted to put the config value into SSM and load it

        // TODO: Do we need to configure the visibility timeout to match terraform? if so, put in SSM parameter
        // TODO: When Spring Cloud AWS goes to version 2.3+, update code to setQueueStopTimeout(...) to same
        //  value as the waitTimeOut, until then we will have exception on shutdown

        return factory;
    }

    @Bean
    @Primary
    public AWSSimpleSystemsManagement ssmClient(TerraformWorkspace terraformWorkspace)
            throws Exception {

        List<TerraformOutput> terraformOutputs = null;
        if (isLocal) {
            terraformOutputs = terraformWorkspace.getOutputs();
        }

        AWSCredentialsProvider credentialsProvider = loadCredentials(terraformOutputs);

        AWSSimpleSystemsManagementClientBuilder builder = AWSSimpleSystemsManagementClientBuilder
                .standard().withCredentials(credentialsProvider);
        return builder.build();
    }
}
