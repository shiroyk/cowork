package doc.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication


@SpringBootApplication
@ConfigurationPropertiesScan
open class DocApplication

fun main(args: Array<String>) {
    runApplication<DocApplication>(*args)
}