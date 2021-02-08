package com.samuraiiway.restrepository.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface HttpRequest {
    String uri();
    String method();
}
