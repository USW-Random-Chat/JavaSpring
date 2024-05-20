package com.USWRandomChat.backend.global.security.aws;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import org.springframework.stereotype.Component;

@Component
public class ParameterStoreUtil {

    private final AWSSimpleSystemsManagement ssmClient;

    public ParameterStoreUtil() {
        this.ssmClient = AWSSimpleSystemsManagementClientBuilder.defaultClient();
    }

    public String getParameter(String name) {
        GetParameterRequest request = new GetParameterRequest()
                .withName(name)
                .withWithDecryption(true);
        GetParameterResult result = ssmClient.getParameter(request);
        return result.getParameter().getValue();
    }
}
