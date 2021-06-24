package com.shiroyk.cowork.coworkadmin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.shiroyk.cowork.coworkadmin.repository.user", mongoTemplateRef = "userMongoTemplate")
public class UserMongoConfiguration {
    @Value("${spring.data.mongodb.user.uri}")
    private String userMongoUri;

    @Bean("userMongoTemplate")
    MongoTemplate userMongoTemplate() {
        return new MongoTemplate(userMongoFactory(userMongoUri));
    }

    @Bean
    public MongoDatabaseFactory userMongoFactory(String uri) {
        return new SimpleMongoClientDatabaseFactory(uri);
    }
}
