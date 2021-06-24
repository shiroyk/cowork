package com.shiroyk.cowork.coworkadmin.config;

import com.shiroyk.cowork.coworkadmin.repository.group.GroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.shiroyk.cowork.coworkadmin.repository.group", mongoTemplateRef = "groupMongoTemplate")
public class GroupMongoConfiguration {
    @Value("${spring.data.mongodb.group.uri}")
    private String groupMongoUri;

    @Bean("groupMongoTemplate")
    MongoTemplate groupMongoTemplate() {
        return new MongoTemplate(groupMongoFactory(groupMongoUri));
    }

    @Bean
    public MongoDatabaseFactory groupMongoFactory(String uri) {
        return new SimpleMongoClientDatabaseFactory(uri);
    }
}
