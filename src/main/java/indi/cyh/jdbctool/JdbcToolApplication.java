package indi.cyh.jdbctool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JdbcToolApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(JdbcToolApplication.class);
        springApplication.run(args);
    }
}
