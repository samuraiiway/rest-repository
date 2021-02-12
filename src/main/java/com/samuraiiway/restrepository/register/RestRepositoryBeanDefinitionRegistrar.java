package com.samuraiiway.restrepository.register;

import com.samuraiiway.restrepository.annotation.RestRepository;
import com.samuraiiway.restrepository.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;


@Slf4j
public class RestRepositoryBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        String basePackage = ((StandardAnnotationMetadata) metadata).getIntrospectedClass().getPackage().getName();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                return metadata.isIndependent() && metadata.isInterface();
            }
        };

        provider.addIncludeFilter(new AnnotationTypeFilter(RestRepository.class));

        for (BeanDefinition beanDefinition : provider.findCandidateComponents(basePackage)) {
            Class beanClass = Class.forName(beanDefinition.getBeanClassName());
            Class advisorClass = ((RestRepository) beanClass.getDeclaredAnnotation(RestRepository.class)).advisor();

            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder
                    .rootBeanDefinition(RepositoryRegister.class)
                    .setFactoryMethod("createProxy")
                    .addConstructorArgValue(beanClass)
                    .addConstructorArgReference(StringUtil.getBeanName(advisorClass.getName()));
            registry.registerBeanDefinition(StringUtil.getBeanName(beanClass.getName()), beanBuilder.getBeanDefinition());
        }
    }
}
