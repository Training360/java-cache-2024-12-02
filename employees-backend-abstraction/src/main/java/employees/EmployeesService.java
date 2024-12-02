package employees;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class EmployeesService {

    private EmployeesRepository repository;

    @Cacheable("employees")
    public List<EmployeeResource> listEmployees() {
        return repository.findAllResources();
    }

    public EmployeeResource findEmployeeById(long id) {
        return toDto(repository.findById(id).orElseThrow(notFountException(id)));
    }

    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeResource createEmployee(EmployeeResource command) {
        Employee employee = new Employee(command.getName());
        repository.save(employee);
        return toDto(employee);
    }

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeResource updateEmployee(long id, EmployeeResource command) {
        Employee employee = repository.findById(id).orElseThrow(notFountException(id));
        employee.setName(command.getName());
        return toDto(employee);
    }

    @CacheEvict(value = "employees", allEntries = true)
    public void deleteEmployee(long id) {
        repository.deleteById(id);
    }

    private EmployeeResource toDto(Employee employee) {
        return new EmployeeResource(employee.getId(), employee.getName());
    }

    private Supplier<EmployeeNotFoundException> notFountException(long id) {
        return () -> new EmployeeNotFoundException("Employee not found with id: %d".formatted(id));
    }

}
