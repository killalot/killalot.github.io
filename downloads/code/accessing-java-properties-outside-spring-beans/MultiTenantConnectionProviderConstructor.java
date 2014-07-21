  private Properties properties;

  public MultiTenantConnectionProvider(){
      properties = new Properties();
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      InputStream stream = loader.getResourceAsStream("filename.properties");
      properties.load(stream);

      defaultDataSource = new ComboPooledDataSource(properties.getProperty("shared_database.name"));
     
     // rest of constructor omitted 
  }
