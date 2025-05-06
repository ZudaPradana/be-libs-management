package zydd.org.libsmanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibsManagementApplication {

    Dotenv dotenv = Dotenv.load();
    public static void main(String[] args) {
        SpringApplication.run(LibsManagementApplication.class, args);
    }

}
