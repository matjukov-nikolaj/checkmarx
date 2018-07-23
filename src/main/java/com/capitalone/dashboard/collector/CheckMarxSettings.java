package com.capitalone.dashboard.collector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import codesecurity.collector.CodeSecuritySettings;

@Component
@ConfigurationProperties(prefix = "checkmarx")
public class CheckMarxSettings extends CodeSecuritySettings{
}
