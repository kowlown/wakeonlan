package com.mystnihon.wakeonlan.configuration;

import com.mystnihon.wakeonlan.PropertyComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertyConfiguration {
    @Bean
    public PropertyComponent getPropertyComponent() {
        return new PropertyComponent();
    }
}
