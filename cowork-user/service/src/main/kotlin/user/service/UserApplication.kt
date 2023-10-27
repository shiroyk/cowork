package user.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication


@SpringBootApplication
@ConfigurationPropertiesScan
open class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}