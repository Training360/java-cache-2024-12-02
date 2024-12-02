package employees;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(EmployeesProperties.class)
public class ClientConfig {
//    @Bean
//    public EmployeesClient employeesClient(RestClient.Builder builder, EmployeesProperties employeesProperties) {
//        var restClient = builder
//                .baseUrl(employeesProperties.getBackendUrl())
//                .build();
//        var factory = HttpServiceProxyFactory
//                .builderFor(RestClientAdapter.create(restClient))
//                .build();
//        return factory.createClient(EmployeesClient.class);
//    }

    @Bean
    public RestClient restClient(EmployeesProperties employeesProperties) {
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .baseUrl(employeesProperties.getBackendUrl())
                .build();
    }




}
