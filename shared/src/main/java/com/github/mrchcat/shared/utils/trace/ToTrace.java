package com.github.mrchcat.shared.utils.trace;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE} )
@Retention(RetentionPolicy.RUNTIME)
public @interface ToTrace {
    String spanName();
    String[] tags() default "";//в формате [\w+:\w+]
}
