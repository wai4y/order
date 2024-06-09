package com.test.order.common;

import java.util.UUID;

public final class IDUtils {

    private IDUtils() {}

    public static Long longUUID() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
}
