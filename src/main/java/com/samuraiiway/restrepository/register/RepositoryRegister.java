package com.samuraiiway.restrepository.register;

import com.samuraiiway.restrepository.advisor.RestRepositoryAdvisor;
import com.samuraiiway.restrepository.annotation.HttpRequest;
import com.samuraiiway.restrepository.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RepositoryRegister implements MethodInterceptor {

    private final RestRepositoryAdvisor restRepositoryAdvisor;

    private RepositoryRegister(RestRepositoryAdvisor restRepositoryAdvisor) {
        this.restRepositoryAdvisor = restRepositoryAdvisor;
    }

    private static final Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\}");

    public static <T> T createProxy(Class<T> type, RestRepositoryAdvisor restRepositoryAdvisor) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(type);
        proxyFactory.addAdvice(new RepositoryRegister(restRepositoryAdvisor));

        return type.cast(proxyFactory.getProxy());
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (invocation.getMethod().isAnnotationPresent(HttpRequest.class)) {
            return execute(invocation, restRepositoryAdvisor);
        }

        if ("toString".equals(invocation.getMethod().getName())) {
            return this.toString();
        }

        return invocation.proceed();
    }

    private Object execute(MethodInvocation invocation, RestRepositoryAdvisor restRepositoryAdvisor) {
        HttpMethod method = resolveMethod(invocation);
        String url = restRepositoryAdvisor.getHostName() + resolveUri(invocation);
        HttpHeaders headers = restRepositoryAdvisor.getHeader();
        Object body = resolveBody(invocation);
        HttpEntity request = new HttpEntity(body, headers);

        try {
            ResponseEntity responseEntity = restRepositoryAdvisor
                    .getRestTemplate()
                    .exchange(url, method, request, resolveTypeReference(invocation));

            restRepositoryAdvisor.handlePostSuccess(responseEntity);
            return responseEntity.getBody();

        } catch (HttpStatusCodeException ex) {
            restRepositoryAdvisor.handleHttpException(ex);

        } catch (Exception ex) {
            restRepositoryAdvisor.handleDefaultException(ex);

        }

        return null;
    }

    private ParameterizedTypeReference resolveTypeReference(MethodInvocation invocation) {
        return ParameterizedTypeReference.forType(invocation.getMethod().getGenericReturnType());
    }

    private Object resolveBody(MethodInvocation invocation) {
        int idx = 0;

        for (Parameter parameter : invocation.getMethod().getParameters()) {
            Annotation annotation = parameter.getAnnotation(RequestBody.class);
            if (annotation != null) {
                return invocation.getArguments()[idx];
            }
            idx++;
        }

        return null;
    }

    private HttpMethod resolveMethod(MethodInvocation invocation) {
        return HttpMethod.resolve(invocation.getMethod().getAnnotation(HttpRequest.class).method());
    }

    private String resolveUri(MethodInvocation invocation) {
        String uri = invocation.getMethod().getAnnotation(HttpRequest.class).uri();

        Matcher matcher = pattern.matcher(uri);

        while(matcher.find()) {
            String replace = matcher.group(0);
            String key = matcher.group(1);

            int idx = 0;
            for (Parameter parameter : invocation.getMethod().getParameters()) {
                if (key.equals(parameter.getName())) {
                    break;
                }
                idx++;
            }

            uri = uri.replace(replace, "" + invocation.getArguments()[idx]);
        }

        return uri;
    }
}
