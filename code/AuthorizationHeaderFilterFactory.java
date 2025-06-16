import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthorizationHeaderFilterFactory extends AbstractGatewayFilterFactory<AuthorizationHeaderFilterFactory.Config> {

    public AuthorizationHeaderFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }
            // Potenziale logica di validazione del token qui
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configurazione specifica del filtro, se necessaria
    }
}