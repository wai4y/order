package com.test.order.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;


public class LatLonValidator implements ConstraintValidator<ValidLatAndLon, String[]> {
    private static final String LATITUDE_REGEX = "^([+\\-])?(?:90(?:\\.0{1,6})?|([1-8]?\\d)(?:\\.\\d{1,6})?)$";
    private static final String LONGITUDE_REGEX = "^([+\\-])?(?:180(?:\\.0{1,6})?|([1-9]?\\d|1[0-7]\\d)(?:\\.\\d{1,6})?)$";

    @Override
    public boolean isValid(String[] values, ConstraintValidatorContext context) {
        if (values == null || values.length != 2 || Arrays.stream(values).anyMatch(Strings::isBlank)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Must provide both latitude and longitude")
                    .addConstraintViolation();
            return false;
        }
        String latitude = values[0];
        if (latitude.isBlank() || !latitude.matches(LATITUDE_REGEX)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Latitude should be String number between -90 and 90, maximum precision is 6 digits")
                    .addConstraintViolation();
            return false;
        }

        String longitude = values[1];
        if (longitude.isBlank() || !longitude.matches(LONGITUDE_REGEX)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Longitude should be String number between -180 and 180, maximum precision is 6 digits")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
