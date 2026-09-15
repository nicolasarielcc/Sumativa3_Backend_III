package cl.duoc.banco.bff.mobile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import cl.duoc.banco.bff.common.http.SslRestClientFactory;

@Configuration
public class CoreClientConfig {

    @Bean
    public RestClient coreRestClient(SslRestClientFactory factory,
                                     @Value("${backend-core.base-url}") String baseUrl) {
        return factory.create(baseUrl);
    }
}
