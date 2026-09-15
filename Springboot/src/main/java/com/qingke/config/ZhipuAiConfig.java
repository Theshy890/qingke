package com.qingke.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "zhipu.ai")
public class ZhipuAiConfig {
    private List<String> apiKeys;
    private String model;
    private String apiUrl;
}