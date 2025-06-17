public class HeaderAuthenticationConverter implements ServerAuthenticationConverter {
	
	private static final String USER_HEADER = "X-Auth-User";
    private static final String ROLES_HEADER = "X-Auth-Roles";
    
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();

        String username = headers.getFirst(USER_HEADER);
        String rolesHeader = headers.getFirst(ROLES_HEADER);

        if (username == null || rolesHeader == null) {
            return Mono.empty();
        }

        List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .toList();

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        
        return Mono.just(authentication);
    }

}