import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.drp")
@MapperScan("com.drp.*.repository")
@EnableFeignClients(basePackages = "com.drp.user.api.feign")
public class DrpBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(DrpBootApplication.class, args);
    }
}
