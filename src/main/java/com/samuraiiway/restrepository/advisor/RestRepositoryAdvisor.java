package com.samuraiiway.restrepository.advisor;

import com.samuraiiway.restrepository.exception.RestRepositoryException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public interface RestRepositoryAdvisor {
    HttpHeaders getHeader();

    String getHostName();

    RestTemplate getRestTemplate();

    default void handlePostSuccess(ResponseEntity responseEntity) {}

    default void handleHttpException(HttpStatusCodeException ex) {
        ex.printStackTrace();
        throw new RestRepositoryException(ex.getStatusCode(), ex.getClass().getName(), new String(ex.getResponseBodyAsByteArray()));
    }

    default void handleDefaultException(Throwable ex) {
        ex.printStackTrace();
        throw new RestRepositoryException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getClass().getName(), ex.getMessage());
    }
}
