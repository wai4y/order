package com.test.order.controller.dto.request;

import com.test.order.controller.dto.request.OrderRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class OrderRequestTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest(name = "test with invalid origin : {0}")
    @MethodSource("invalidLocation")
    public void testOrderRequestOriginError(String[] origin) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrigin(origin);
        orderRequest.setDestination(new String[]{"22", "22"});

        Set<ConstraintViolation<OrderRequest>> validate = validator.validate(orderRequest);
        assertFalse(validate.isEmpty());
    }

    static Stream<Arguments> invalidLocation() {
        return Stream.of(
                Arguments.of((Object) new String[]{null, null}),
                Arguments.of((Object) new String[]{"", ""}),
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"abc", "def"}),
                Arguments.of((Object) new String[]{"90", "def"}),
                Arguments.of((Object) new String[]{"90", "181"}),
                Arguments.of((Object) new String[]{"91", "181"}),
                Arguments.of((Object) new String[]{"abc", "81"}),
                Arguments.of((Object) new String[]{"-92", "81"}),
                Arguments.of((Object) new String[]{"-90", "-181"}),
                Arguments.of((Object) new String[]{"-20.1122334", "-11"}),
                Arguments.of((Object) new String[]{"-20.112233", "-18.1122334"})
        );
    }


    @ParameterizedTest(name = "test with invalid destination : {0}")
    @MethodSource("invalidLocation")
    public void testOrderRequestDestinationError(String[] destination) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrigin(new String[]{"90", "90"});
        orderRequest.setDestination(destination);

        Set<ConstraintViolation<OrderRequest>> validate = validator.validate(orderRequest);
        assertFalse(validate.isEmpty());
    }


    @ParameterizedTest(name = "test with valid destination and different origin : {0}")
    @MethodSource("validLocation")
    public void testValidOrderRequestOrigin(String[] origin) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrigin(origin);
        orderRequest.setDestination(new String[]{"90", "90"});

        Set<ConstraintViolation<OrderRequest>> validate = validator.validate(orderRequest);
        assertTrue(validate.isEmpty());
    }

    @ParameterizedTest(name = "test with valid origin and different destination : {0}")
    @MethodSource("validLocation")
    public void testValidOrderRequestDestination(String[] destination) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrigin(new String[]{"90", "90"});
        orderRequest.setDestination(destination);

        Set<ConstraintViolation<OrderRequest>> validate = validator.validate(orderRequest);
        assertTrue(validate.isEmpty());
    }

    static Stream<Arguments> validLocation() {
        return Stream.of(
                Arguments.of((Object) new String[]{"1", "1"}),
                Arguments.of((Object) new String[]{"-1", "-1"}),
                Arguments.of((Object) new String[]{"45.232323", "45.223344"}),
                Arguments.of((Object) new String[]{"89.112233", "180"}),
                Arguments.of((Object) new String[]{"-90", "-180"}),
                Arguments.of((Object) new String[]{"-90", "100.112233"}),
                Arguments.of((Object) new String[]{"90", "-100"}),
                Arguments.of((Object) new String[]{"80.123", "-100.123"})
        );
    }

}
