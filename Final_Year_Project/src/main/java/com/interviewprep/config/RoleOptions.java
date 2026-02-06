package com.interviewprep.config;

import java.util.List;

/**
 * Predefined professional roles for the Role Selection dropdown.
 * "Other" allows custom text input stored as-is.
 */
public final class RoleOptions {

    public static final String OTHER_LABEL = "Other";

    public static final List<String> PREDEFINED_ROLES = List.of(
            "Java Full Stack Developer",
            "Java Backend Developer",
            "Spring Boot Microservices Engineer",
            "Java Enterprise Application Developer",
            "Senior Java Developer",
            "Java + React Full Stack Engineer",
            "Spring Boot + Hibernate Specialist",
            OTHER_LABEL
    );

    private RoleOptions() {}
}
