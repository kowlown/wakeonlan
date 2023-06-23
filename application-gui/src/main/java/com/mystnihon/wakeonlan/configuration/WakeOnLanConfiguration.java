package com.mystnihon.wakeonlan.configuration;

import com.mystnihon.wakeonlan.WakeOnLan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WakeOnLanConfiguration {

    @Bean
    public WakeOnLan wakeOnLan(){
        return new WakeOnLan();
    }
}
