package com.safely.batch.connector.components.external;

import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.client.AuthorizationClient;
import com.safely.batch.connector.entity.AuthenticationResponseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoadPmsAuthTokenService {

    private static final Logger log = LoggerFactory.getLogger(LoadPmsAuthTokenService.class);
    private final AuthorizationClient authorizationClient;

    public LoadPmsAuthTokenService(AuthorizationClient authorizationClient) {
        this.authorizationClient = authorizationClient;
    }

    public void execute(JobContext jobContext) throws Exception{
        try {

            log.info("OrganizationId: {}. Retrieving Pms Token from RealTimeRental.", jobContext.getOrganizationId());

            String username = jobContext.getOrganization().getOrganizationSourceCredentials().getAccountKey();
            String password = jobContext.getOrganization().getOrganizationSourceCredentials().getAccountPassword();

            AuthenticationResponseToken token = authorizationClient.getPmsToken(username, password);

            if (token == null) {
                String msg = String.format("OrganizationId: %s. Error occurred while retrieving authentication from PMS.", jobContext.getOrganizationId());
                log.error(msg);
                throw new Exception(msg);
            }
            jobContext.setToken(token.getAccessToken());

        } catch (Exception ex) {
            String msg = String
                    .format("OrganizationId: %s. Failed to retrieve an authentication token for the organization with name: %s. Error message: %s",
                            jobContext.getOrganizationId(), jobContext.getOrganization().getName(), ex.getMessage());
            log.error(msg, ex);
            throw ex;
        }
    }
}
