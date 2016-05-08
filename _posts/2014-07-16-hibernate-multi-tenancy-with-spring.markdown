---
layout: post
title: "Hibernate Multi Tenancy with Spring"
date: 2014-07-16 20:18:54 +0100
comments: true
categories: [Spring, Hibernate, Multi Tenancy, Java]
published: true
---

When creating a Multi-Tenant application there are three different strategies which are available to a development team. This post will demonstrate a possible implementation of the separate database approach.

<!-- more -->

## Prerequisites

- Basic knowledge of hibernate and its configuration
- Basic knowledge of Spring MVC
- Spring Security

## Dependencies

- Hibernate 4.3
- Spring 4.0
- C3P0 0.9.2

## Creating the Shared Database

If each tenant is accessing the application via the same URL, you would typically have a master or shared database which would store the full list of users each having a reference to their tenant database.

When a user attempts to login to the application, you would first connect to the shared database to authenticate them e.g. using spring security, and assertain which database they need to connect to.

The shared database will need a minimum of 2 tables: 1 to store the users and another to store the databases. Here is two entity classes to represent this.

``` java
@Table
@Entity
public class Database{

	@Id
	protected Long id;

	private String name;

	/* Optional fields */
	private String version;

	private String username;

	private String password;

	private String server; // if databases are held in multiple servers

	// getters and setters omitted
}
```

``` java
public class ServerUser{
	@Id
	protected Long id;

	private String username;

	private String password;

	private Boolean administrator;

	@JoinColumn
	@ManyToOne
	private Database database;
}
```

As an alternative to the shared database you could provide each tenant with a separate URL or make the users provide additional information during login, but I won't go into details on how to do this here.

## Hibernate XML Configuration

Lets move on to configuring hibernate for multi-tenancy. In this case I will be using XML but it can be easily converted to a Java configuration class. First we'll look at what the configuration may look without multi-tenancy setup so you can see the difference.

``` xml
<!-- beans Root Element omitted -->

<bean id="datasource" class="org.apache.commons.dbcp.BasicDataSource">
	<property name="initialSize" value="16" />
	<property name="minIdle" value="16" />
	<property name="maxWait" value="10000" />
	<!-- replace with your driver class -->
	<property name="driverClassName" value="com.microsoft.sqlserver.jdbc.SQLServerDriver" />
	<!-- replace with your database url -->
	<property name="url" value="jdbc:sqlserver://localhost;databaseName=appDb" />
	<property name="username" value="username" />
	<property name="password" value="password" />
</bean>

<bean id="sessionFactory" class="org.springframework.orm.hibernate4.LocalSessionFactoryBean">
	<property name="packagesToScan" value="your.models.package"/>
	<property name="dataSource" ref="datasource" />
	<property name="hibernateProperties">
		<props>
			<!-- Change to suit your database -->
			<prop key="hibernate.dialect">org.hibernate.dialect.SQLServer2012Dialect</prop>
		</props>
	</property>
</bean>
```

``` xml
<bean id="sessionFactory" class="org.springframework.orm.hibernate4.LocalSessionFactoryBean">
	<property name="packagesToScan" value="your.models.package" />
	<property name="hibernateProperties">
		<props>
			<prop key="hibernate.dialect">org.hibernate.dialect.SQLServer2012Dialect</prop>				
			<prop key="hibernate.multiTenancy">DATABASE</prop>
			<prop key="hibernate.tenant_identifier_resolver">package.to.your.CurrentTenantIdentifierResolverImpl</prop>
			<prop key="hibernate.multi_tenant_connection_provider">package.to.your.MultiTenantConnectionProvider</prop>
		</props>
	</property>
</bean>
```

As you can see, additional properties have been provided to the session factory so that it is configured for multi-tenancy. You can refer to the 'Hibernate Multi-tenancy' link in the references to understand what these do.

What you will also notice is that the datasource bean has been removed from the configuration. This will be initialised in the MultiTenantConnectionProvider.

## CurrentTenantIdentifierResolver

``` java
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
```

This class is used by the MultiTenantConnectionProvider to ascertain the correct tenant identifier for the current hibernate session that has been opened. In this case spring security has been utilised to obtain the database name, which is the tenant identifier that is being used. If the current user is not authenicated, such as when first logging into the server, then the shared database name is given as the identifier.

A custom implementation of the org.springframework.security.core.userdetails.User class - CustomUserDetails has been used in order to store the database name during authentication so that it can be later retrieved here.

## MultiTenantConnectionProvider

``` java
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
```

This class provides hibernate with the correct datasource depending on the value of the tenant identifier given to it. During initialisation the class creates the default pooled datasource which is pointing to the shared database. The selectDataSource method uses the static method pooledDataSourceByName of the C3P0Registry to obtain the datasource. If a datasource does not exist for the tenant identifier, then it is lazy initialised and returned. The tenant identifier is provided to the datasource when it is created so that it can be passed to the C3P0Registry as a lookup field when it is needed to be retreived again.

## HibernateTransactionManager (Optional)

If you are using Spring's HibernateTransactionManager, you will find that when you start up your application it will throw a NullPointerException from the org.springframework.orm.hibernate4.SessionFactoryUtils.getDataSource() method. This is because the dataSource property is no longer being specified in the sessionFactory configuration.

To prevent this exception from occuring, you can add an additional property to its configuration:

``` xml
<property name="autodetectDataSource" value="false"/>
```
## Usage

You can open a hibernate session as you normally expect but now it will interogate the CurrentTenantIdentifierResolver in the background.

``` java
Session session = sessionFactory.openSession();
```

You can also specify the tenant identifier manually, which can be useful when authenticating the user or for system administrator methods.

``` java
Session session = sessionFactory.withOptions().tenantIdentifier(tenantId).openSession();
```

If you are using Spring's HibernateTransactionManager, again the method of obtaining a session doesn't need to change.

``` java
Session session = sessionFactory.getCurrentSession();
```

### References and Further Reading

- <a target="_blank" href="https://docs.jboss.org/hibernate/core/4.3/devguide/en-US/html/ch16.html">Hibernate Multi-tenancy</a>
- <a target="_blank" href="http://www.mchange.com/projects/c3p0/">C3p0 datasource connection pooling</a>
- <a target="_blank" href="http://www.slideshare.net/seges/multitenancy-in-java">Multi-tenancy in Java</a>
