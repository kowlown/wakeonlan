package com.mystnihon.wakeonlan.configuration;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dizitart.no2.Nitrite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NitriteConfiguration {

    @Bean
    public Nitrite nitriteDB() {
        return Nitrite.builder()
                .compressed()
                .registerModule(new JavaTimeModule())
                .filePath("./local.db")
                .openOrCreate("user", "password");
    }
}
