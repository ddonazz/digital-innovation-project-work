@Component
public class AuthorizationHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthorizationHeaderGatewayFilterFactory.Config> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationHeaderGatewayFilterFactory.class);
	
    private final ValidateJwtUtil jwtUtil;
    private final RouterValidator routerValidator;

    public AuthorizationHeaderGatewayFilterFactory(ValidateJwtUtil jwtUtil, RouterValidator routerValidator) {
        super(Config.class); 
        this.jwtUtil = jwtUtil;
        this.routerValidator = routerValidator;
    }

    // La classe Config e' vuota perche' le dipendenze sono iniettate nella factory,
    // e non ci sono parametri specifici da passare tramite YAML a questo filtro.
    public static class Config {
        // Nessun campo necessario qui per la tua logica attuale
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routerValidator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    LOG.warn("Missing Authorization header for secured route.");
                    return response.setComplete();
                }

                String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
                String token = authHeader.replace("Bearer ", "");

                try {
                    jwtUtil.validateToken(token);
                    Claims claims = jwtUtil.extractAllClaims(token);

                    List<?> rawAuthorities = claims.get("authorities", List.class);
                    if (rawAuthorities != null) {
                        List<String> authorities = rawAuthorities.stream()
                                .map(Object::toString)
                                .toList();

                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-Auth-User", claims.getSubject())
                                .header("X-Auth-Roles", String.join(",", authorities))
                                .build();

                        LOG.info("AuthorizationHeaderFilter applied. User: {}", claims.getSubject());
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    }
                } catch (Exception e) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    LOG.warn("Invalid Authorization header: {}", e.getMessage());
                    return response.setComplete();
                }
            }
            return chain.filter(exchange);
        };
    }

}
