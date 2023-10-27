package user.service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager


@Configuration
open class Config {
    @Bean
    open fun transactionManager(dbFactory: MongoDatabaseFactory) = MongoTransactionManager(dbFactory)
}