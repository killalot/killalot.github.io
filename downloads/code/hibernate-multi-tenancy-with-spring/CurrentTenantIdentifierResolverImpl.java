// imports omitted

public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {
	
	@Override
	public String resolveCurrentTenantIdentifier() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();				
		String database = "shared";
		if(authentication != null && authentication.getPrincipal() instanceof CustomUserDetails){
			CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
			database = user.getDatabaseName();
		}
		return database;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}
