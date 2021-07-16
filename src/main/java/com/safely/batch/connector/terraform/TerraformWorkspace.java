package com.safely.batch.connector.terraform;


import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class TerraformWorkspace {

    private static final String profileName = "safely";

    @Autowired
    private ObjectMapper objectMapper;

    public List<TerraformOutput> getOutputs() throws Exception {
        String workspace = getLocalTerraformWorkspace();
        TerraformBackendConfig backendConfig = getLocalTerraformBackendConfig();
        TerraformRemoteState remoteState = getRemoteStateFile(backendConfig, workspace);

        List<TerraformOutput> outputs = remoteState.getOutputs().entrySet().stream()
                .map(x -> new TerraformOutput(x.getKey(), x.getValue().getType(), x.getValue().getValue()))
                .collect(Collectors.toList());

        return outputs;
    }

    private String getLocalTerraformDirectory() {
        String currentDirectory = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
        Path terraformDirectory = Paths.get(currentDirectory, "src/terraform/src/.terraform");
        return terraformDirectory.toAbsolutePath().toString();
    }

    private AWSCredentialsProvider loadCredentials(String region, String roleArn) {
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceAsyncClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider(profileName))
                .withRegion(region)
                .build();

        String sessionName = String.format("terraform-workspace-%s", LocalDateTime.now().toString())
                .replace(':', '.').replace(' ', '-');
        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest().withDurationSeconds(3600)
                .withRoleArn(roleArn)
                .withRoleSessionName(sessionName);

        AssumeRoleResult assumeRoleResult = stsClient.assumeRole(assumeRoleRequest);
        Credentials credentials = assumeRoleResult.getCredentials();

        return new AWSStaticCredentialsProvider(
                new BasicSessionCredentials(credentials.getAccessKeyId(), credentials.getSecretAccessKey(),
                        credentials.getSessionToken()));
    }

    private TerraformRemoteState getRemoteStateFile(TerraformBackendConfig config,
                                                    String workspace) throws Exception {
        String roleArn = config.getBackend().getConfig().getRole_arn();
        String region = config.getBackend().getConfig().getRegion();
        String bucketName = config.getBackend().getConfig().getBucket();
        String bucketKey = String
                .format("env:/%s/%s", workspace, config.getBackend().getConfig().getKey());

        AWSCredentialsProvider credentialsProvider = loadCredentials(region, roleArn);

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider).withRegion(region).build();

            S3Object response = s3Client.getObject(bucketName, bucketKey);
            String remoteStateContents = StreamUtils
                    .copyToString(response.getObjectContent(), StandardCharsets.UTF_8);
            return objectMapper.readValue(remoteStateContents, TerraformRemoteState.class);
        } catch (Exception ex) {
            throw new Exception(String.format(
                    "The '%s' workspace does not exist. Please run terraform plan/apply to create it.",
                    workspace));
        }
    }

    private String getLocalTerraformWorkspace() throws Exception {
        String directory = getLocalTerraformDirectory();
        Path filePath = Paths.get(directory, "environment").toAbsolutePath();

        try {
            return Files.readString(filePath);

        } catch (Exception ex) {
            throw new Exception(String
                    .format("The '%s' file does not exist. Please run terraform plan/apply to create it.",
                            filePath.toString()));
        }
    }

    private TerraformBackendConfig getLocalTerraformBackendConfig() throws Exception {
        String directory = getLocalTerraformDirectory();
        Path filePath = Paths.get(directory, "terraform.tfstate").toAbsolutePath();

        try {
            String json = Files.readString(filePath);

            TerraformBackendConfig config = objectMapper.readValue(json, TerraformBackendConfig.class);
            return config;
        } catch (Exception ex) {
            throw new Exception(String
                    .format("The '%s' file does not exist. Pleas run terraform plan/apply  to create it.",
                            filePath.toString()));
        }
    }
}
