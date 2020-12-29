package com.example.demo._5_4_2;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;

public class Test {

	private static Logger log = LoggerFactory.getLogger(Test.class);

	public static void main(String[] args) {
		AnnotationMetadata annotationMetadata = AnnotationMetadata.introspect(ServiceAspect.class);
		Set<String> set = annotationMetadata.getAnnotationTypes();
		for (String string : set) {
			log.info("{}", string);
		}

		AnnotatedGenericBeanDefinition annotatedGenericBeanDefinition = new AnnotatedGenericBeanDefinition(
				ServiceAspect.class);
		for (MergedAnnotation<Annotation> obj : annotatedGenericBeanDefinition.getMetadata().getAnnotations()) {
			log.info("{}", obj.asAnnotationAttributes());
		}
	}
}
