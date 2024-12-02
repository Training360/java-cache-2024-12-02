package employees;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

public interface EmployeesRepository extends JpaRepository<Employee, Long> {

    @Query("select new employees.EmployeeResource(e.id, e.name) from Employee e")
    @QueryHints(
            @QueryHint(name = "org.hibernate.cacheable", value = "true")
    )
    List<EmployeeResource> findAllResources();
}
