---
layout: post
title: "Accessing java properties outside spring beans"
date: 2014-07-21 06:48:04 +0100
comments: true
categories: spring 4, java properties
---
<p>This is a kind of follow up from my <a href="{% post_url 2014-07-16-hibernate-multi-tenancy-with-spring %}">previous post</a>. In which I describe an example implementation of a Hibernate MultiTenantConnectionProvider and CurrentTenantIdentifierResolver. Later after writing I thought it would better to abstract the database configuration in these two files into a .properties file - so that the properties can be reused in other areas of the application such as when authenicating a user.</p>

<!-- more -->
<p>Normally when you wish to use a .properties file in a spring application, you define a property placeholder in your context xml (or Java config) like so:</p>
``` xml
<context:property-placeholder location="classpath:filename.properties" />

```

<p>Then if you want to access the properties in a spring bean , you can use the @Value annotation in the bean class to inject the required property.</p>

``` java
@Value("${property.value}")
private String property;
```
<p>Now this is an easy way to access your properties, however there is a situation which I've discovered that requires a different approach. If the class you wish to pass the properties to does not get initialised as a spring bean - such as the MultiTenantConnectionProvider in my previous post, then you cannot use the @Value annotation. In this case it is because only the class name is given to hibernate which it will then make an instance of internally.</p>

<p>A workaround for this is to access the properties file using a ClassLoader inside the MultiTenantConnectionProvider constructor.</p>

``` java
private Properties properties;

public MultiTenantConnectionProvider(){
    properties = new Properties();
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream stream = loader.getResourceAsStream("filename.properties");
    properties.load(stream);

    defaultDataSource = new ComboPooledDataSource(properties.getProperty("shared_database.name"));

   // rest of constructor omitted
}
```

<p>This way you still access your properties anywhere inside a non-bean class.</p>
