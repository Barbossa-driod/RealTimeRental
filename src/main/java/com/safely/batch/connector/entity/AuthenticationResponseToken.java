package com.safely.batch.connector.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationResponseToken {

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty(".issued")
    private String issued;

    @JsonProperty(".expires")
    private String expires;
}
