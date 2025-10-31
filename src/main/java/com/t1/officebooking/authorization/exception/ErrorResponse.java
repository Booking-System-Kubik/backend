package com.t1.officebooking.authorization.exception;

import java.util.Map;

public record ErrorResponse(
        String message,
        Map<String, String> errors
) {}
