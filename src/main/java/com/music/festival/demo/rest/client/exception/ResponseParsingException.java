package com.music.festival.demo.rest.client.exception;

/**
 * Custom exception to be thrown when API response cannot be parsed.
 */
public class ResponseParsingException extends Exception {
    public ResponseParsingException(String message) {
        super(message);
    }
}
