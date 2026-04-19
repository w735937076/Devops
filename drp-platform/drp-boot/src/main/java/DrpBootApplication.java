import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.drp")
@MapperScan("com.drp.*.repository")
public class DrpBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(DrpBootApplication.class, args);
    }
}
