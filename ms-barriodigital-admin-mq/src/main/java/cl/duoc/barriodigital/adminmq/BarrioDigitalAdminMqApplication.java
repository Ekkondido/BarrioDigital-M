package cl.duoc.barriodigital.adminmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BarrioDigitalAdminMqApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarrioDigitalAdminMqApplication.class, args);
    }
}
