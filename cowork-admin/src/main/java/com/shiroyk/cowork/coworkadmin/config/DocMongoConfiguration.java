package com.shiroyk.cowork.coworkadmin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.shiroyk.cowork.coworkadmin.repository.doc", mongoTemplateRef = "docMongoTemplate")
public class DocMongoConfiguration {
    @Value("${spring.data.mongodb.doc.uri}")
    private String docMongoUri;

    @Primary
    @Bean("docMongoTemplate")
    MongoTemplate docMongoTemplate() {
        return new MongoTemplate(docMongoFactory(docMongoUri));
    }

    @Bean
    @Primary
    MongoDatabaseFactory docMongoFactory(final String mongo) {
        return new SimpleMongoClientDatabaseFactory(mongo);
    }
}
