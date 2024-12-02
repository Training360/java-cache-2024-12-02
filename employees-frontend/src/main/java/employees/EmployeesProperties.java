package employees;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "employees")
@Data
public class EmployeesProperties {

    private String backendUrl;
}
