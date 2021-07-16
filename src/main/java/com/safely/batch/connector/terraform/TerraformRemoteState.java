package com.safely.batch.connector.terraform;

import java.util.Map;
import lombok.Data;

@Data
public class TerraformRemoteState {

    private Map<String, Output> outputs;
}
