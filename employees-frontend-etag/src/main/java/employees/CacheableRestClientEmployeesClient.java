package employees;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CacheableRestClientEmployeesClient implements EmployeesClient {

    private final RestClient restClient;

    private final CacheManager cacheManager;

    @Override
    public List<Employee> listEmployees() {
        var wrapper = cacheManager.getCache("employees-etag").get("employees");
        EtaggedValue<List<Employee >> value = null;
        if (wrapper != null) {
            value = (EtaggedValue<List<Employee >>) wrapper.get();
        }

        var request = restClient
                .get()
                .uri("/api/employees");
        if (value != null) {
            request
                    .header("If-None-Match", "%s".formatted(value.getEtag()));
        }
        ResponseEntity<List<Employee>> response = request.retrieve()
                .toEntity(new ParameterizedTypeReference<List<Employee>>() {});

        if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
            log.info("Not modified");
            return value.getValue();
        }
        else {
            log.info("Modified");
            cacheManager.getCache("employees-etag").put("employees",
                    new EtaggedValue<>(response.getHeaders().getETag(), response.getBody()));
            return response.getBody();
        }
    }

    @Override
    public Employee createEmployee(Employee employee) {
        return restClient.post().uri("/api/employees").body(employee).retrieve().body(Employee.class);
    }
}
