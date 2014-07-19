public class MultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl  {

	private static final long serialVersionUID = 6241633589847209550L;
	
	private ComboPooledDataSource defaultDataSource; 

	public MultiTenantConnectionProvider(){
		defaultDataSource = new ComboPooledDataSource("shared");
		defaultDataSource.setJdbcUrl("jdbc:sqlserver://localhost;databaseName=shared");
		defaultDataSource.setUser("username");
		defaultDataSource.setPassword("password");
		defaultDataSource.setInitialPoolSize(16);
		defaultDataSource.setMaxConnectionAge(10000);
		try {
			defaultDataSource.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected DataSource selectAnyDataSource(){		
		return defaultDataSource;
	}

	@Override
	protected DataSource selectDataSource(String tenantIdentifier) {
		PooledDataSource pds = C3P0Registry.pooledDataSourceByName(tenantIdentifier);
		if(pds==null){
			cpds = new ComboPooledDataSource(tenantIdentifier);
			cpds.setJdbcUrl("jdbc:sqlserver://localhost;databaseName=" + tenantIdentifier);
			cpds.setUser("username");
			cpds.setPassword("password");
			cpds.setInitialPoolSize(16);
			cpds.setMaxConnectionAge(10000);
			try {
				cpds.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return cpds;
			
		}
		return pds;


	}

}