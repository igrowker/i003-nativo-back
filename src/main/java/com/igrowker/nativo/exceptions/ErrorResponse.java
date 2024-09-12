package com.igrowker.nativo.exceptions;

public record ErrorResponse(
        int statusCode,
        String message
) {

}