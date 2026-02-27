package iee.ihu.gr.pricemonitoringbackend.service

import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

@Profile("generate")
@Component
class GeneratedProfileCleaner {

    @PreDestroy
    private fun deleteTempFiles(){
        Files.deleteIfExists(Path.of("create.sql"))
        Files.deleteIfExists(Path.of("drop.sql"))

    }

}