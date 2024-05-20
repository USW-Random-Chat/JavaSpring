package com.USWRandomChat.backend.global.security.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ParameterStoreConfig {

    @Autowired
    private ParameterStoreUtil parameterStoreUtil;

    @Autowired
    private ConfigurableEnvironment env;

    @Bean
    @Primary
    public void loadParameters() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("mail.username", parameterStoreUtil.getParameter("/suchat/mail/username"));
        propertyMap.put("mail.password", parameterStoreUtil.getParameter("/suchat/mail/password"));
        propertyMap.put("jwt.secret.key", parameterStoreUtil.getParameter("/suchat/jwt-secret-key"));

        env.getPropertySources().addFirst(new MapPropertySource("aws-parameters", propertyMap));
    }
}
