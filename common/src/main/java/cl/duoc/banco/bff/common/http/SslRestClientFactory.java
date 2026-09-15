package cl.duoc.banco.bff.common.http;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Fabrica de {@link RestClient} configurado para confiar en el certificado
 * autofirmado del entorno (HTTPS). Utiliza el mismo keystore PKCS12 como
 * material de confianza (truststore) y timeouts explicitos para "fail fast".
 *
 * <p><b>Nota:</b> pensado para desarrollo local. En AWS se debe reemplazar el
 * certificado autofirmado por un certificado emitido por ACM/CA y eliminar la
 * confianza ciega del keystore local.</p>
 */
@Component
public class SslRestClientFactory {

    private final String keystoreLocation;
    private final String keystorePassword;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public SslRestClientFactory(
            @Value("${app.security.keystore:classpath:keystore/keystore.p12}") String keystoreLocation,
            @Value("${app.security.keystore-password:banco123}") String keystorePassword,
            @Value("${app.http.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${app.http.read-timeout-ms:5000}") int readTimeoutMs) {
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * Crea un {@link RestClient} apuntando a {@code baseUrl} con soporte TLS
     * (confia en el certificado autofirmado local) y timeouts explicitos.
     */
    public RestClient create(String baseUrl) {
        try {
            SSLContext sslContext = buildSslContext();
            HttpClient httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                    .build();

            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

            return RestClient.builder()
                    .baseUrl(baseUrl)
                    .requestFactory(requestFactory)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo configurar el cliente HTTPS", e);
        }
    }

    private SSLContext buildSslContext() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        Resource resource = new DefaultResourceLoader().getResource(keystoreLocation);
        try (InputStream in = resource.getInputStream()) {
            trustStore.load(in, keystorePassword.toCharArray());
        }

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
}
