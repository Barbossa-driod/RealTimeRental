package com.safely.batch.connector.terraform;

import lombok.Data;

@Data
public class TerraformOutput {
    private String key;
    private String type;
    private String stringValue;

    public TerraformOutput(String key, String type, String stringValue) {
        this.key = key;
        this.type = type;
        this.stringValue = stringValue;
    }
}
