package com.empik.recruitment.couponservice.interfaces;

import com.empik.recruitment.couponservice.interfaces.deserializer.LowerCaseDeserializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = LowerCaseDeserializer.class)
public @interface LowerCase {
}