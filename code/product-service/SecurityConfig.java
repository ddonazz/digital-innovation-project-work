@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity 
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(getAuthenticationManager());
        authenticationWebFilter.setServerAuthenticationConverter(new HeaderAuthenticationConverter());

        return http
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().authenticated()
                )
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .build();
    }

    /**
     * Un AuthenticationManager che si fida dell'autenticazione creata dal converter.
     * Dato che il nostro converter crea un token gia' "autenticato" (con ruoli),
     * questo manager non deve fare complesse validazioni.
     * Restituiamo un manager che accetta semplicemente l'oggetto.
     */
    private ReactiveAuthenticationManager getAuthenticationManager() {
        return Mono::just;
    }

}
