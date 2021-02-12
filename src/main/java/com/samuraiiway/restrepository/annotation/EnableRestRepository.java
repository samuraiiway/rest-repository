package com.samuraiiway.restrepository.annotation;

import com.samuraiiway.restrepository.register.RestRepositoryBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RestRepositoryBeanDefinitionRegistrar.class)
public @interface EnableRestRepository {
}
