package employees;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EmployeesFrontendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeesFrontendApplication.class, args);
	}

}
