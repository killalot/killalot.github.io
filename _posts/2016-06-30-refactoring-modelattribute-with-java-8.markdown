---
layout: post
title: "Refactoring Spring @ModelAttribute Method With Java 8"
date: 2016-06-30T06:34:46+01:00
excerpt: "ModelAttribute methods in Spring controllers that obtain database data using path variables can be ugly in Spring 3/4. In this post I demonstrate how to make your ModelAttribute methods more succinct and readable using Java 8 Optional and lambdas."
keywords: "Spring 4, Java 8, @ModelAttribute, controller, mvc, pathvariable, optional, refactoring"
---

## Introduction

In Spring MVC you can annotate a public method in a controller with `@ModelAttribute`. By doing so the method is run before any `@RequestMapping` method (how Spring controllers deal with requests) is run. Methods annotated as such are used to set one or more model attributes that can be later used in the controller itself or in a template engine such as JSP. In this post I would like to show how you can refactor a ModelAttribute method to take advantage of Spring 4's support of Java 8 Optionals which coupled with lambdas can provide you with a concise and readable solution.

## The Staff Member Example

As an example to help us along, consider a HR application. In this application as you might expect is a requirement to insert and edit Staff member records. Part of the implementation for this is the spring controller which responds to the following paths:

- `/staff` GET (list of staff)
- `/staff/{id}/edit` GET (edit staff member form)
- `/staff/{id}` POST (update staff member)
- `/staff/{id}` DELETE (delete staff member)
- `/staff` POST (create staff member)
- `/staff/new` GET (new staff member form)

`{id}` is the staff members unique identifier in the database. Data is submitted to the controller via a typical HTML form - we won't consider AJAX here. To narrow down the possible solutions some more I will say that the form does not contain all inputs for the staff member entity. A session attribute `username` is needed to be set to a new staff member instance for it to be valid.

## Implementation Prior To Java 8

A primary part of the spring controller is the ModelAttribute method. Lets look at how this could be written prior to Java 8.

``` java
@ModelAttribute("staffMember")
public StaffMember loadStaff(@PathVariable Map<String,String> pathVariables, HttpSession session) {
    StaffMember staffMember;
    if (pathVariables.containsKey("id")) {
        Long id = Long.parseLong(pathVariables.get("id"));
        staffMember = staffMemberDao.findById(id);
    } else {
        staffMember = new StaffMember();
        staffMember.setCreatedBy((String)session.getAttribute("username"));
    }
    return staffMember;        
}
```

Here you can see two distinct paths of the method. The annotated `pathVariables` parameter is being used as a means to check what action the user is performing inside the controller. If the `id` path variable exists then the user has either submitted a update form or visited the edit page. So in this situation we need to obtain the existing staff member from the database. When the `id` path variable isn't present then we know the user has visited the new form or index page or submitted a create form. In this case we create a new instance of the `StaffMember` object and set the `createdBy` field to the value present in the session (we will assume the session attribute is guaranteed to be there).

## Refactoring The Method To Use Java 8

With Spring 4.1 and above now supporting `Optional<T>` parameters annotated by `@PathVariable` , we can rewrite the above code like so:

``` java
@ModelAttribute("staffMember")
public StaffMember loadStaff(@PathVariable("id") Optional<Long> idOpt, HttpSession httpSession) {
    return idOpt.map(id -> staffMemberDao.findById(id))
            .orElseGet(() -> {
                StaffMember staffMember = new StaffMember();
                staffMember.setCreatedBy((String)session.getAttribute("username"));
                return staffMember;
            });
}
```

The first difference you'll be able to see is that we are no longer obtaining all the available path variables. Since Spring does not allow you to have a path variable parameter that is null (Spring will throw an exception), we had to bring in a Map of the path variables and check if `id` was present. In Spring 4.1+ we can now wrap our sometimes null path variable with the Optional class. This is explicitly stating that the path variable could be missing, providing better readability.

With access to an Optional instance, we can make use of its available methods. The lambda inside the map method is run only when the `id` path variable is present. You can think of it as a replacement of the if block in the previous code. The else block functionality has been replaced by the `orElseGet` method. This takes a `Supplier<T>` function and runs when `ipOpt` is empty, in other words when the `id` variable is not present in the path.

## Summary

You can see how the second code example is less verbose than the first and is also easier to read. We have hidden the null check of the path variable and casting it to a Long which is now performed internally.

Hopefully this refactoring example has given you a glimpse of what is now possible using Java 8 and Spring 4.
