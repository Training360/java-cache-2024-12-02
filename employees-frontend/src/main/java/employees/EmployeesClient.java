package employees;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/api/employees")
public interface EmployeesClient {

    @GetExchange
    List<Employee> listEmployees();

    @PostExchange
    Employee createEmployee(@RequestBody Employee employee);

    @GetMapping
    Employee findEmployeeById(@PathVariable Long employeeId);
}
