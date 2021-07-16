package com.safely.batch.connector.terraform;

import lombok.Data;

@Data
public class BackendConfig {

    private String bucket;
    private String key;
    private String region;
    private String role_arn;
}
