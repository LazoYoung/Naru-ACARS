package com.flylazo.naru_acars.servlet.socket;

public enum SocketError {
    OFFLINE("Server is offline!"),
    API_KEY_IN_USE("API key is in use!"),
    API_KEY_INVALID("API key is invalid!"),
    FATAL_ERROR("Fatal error!"),
    ;

    public final String message;

    SocketError(String message) {
        this.message = message;
    }
}
