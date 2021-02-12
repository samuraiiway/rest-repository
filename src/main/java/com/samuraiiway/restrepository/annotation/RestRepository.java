package com.samuraiiway.restrepository.annotation;

import com.samuraiiway.restrepository.advisor.RestRepositoryAdvisor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestRepository {
    Class<? extends RestRepositoryAdvisor> advisor();
}
