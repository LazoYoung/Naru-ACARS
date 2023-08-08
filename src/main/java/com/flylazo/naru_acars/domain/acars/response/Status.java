package com.flylazo.naru_acars.domain.acars.response;

public enum Status {
    SUCCESS(200),
    BAD_REQUEST(400),
    FORBIDDEN(403),
    NOT_FOUND(404),
    TIMEOUT(408),
    BEFORE_FLIGHT(440),
    BAD_STATE(450),
    CLIENT_ERROR(499),
    SERVER_ERROR(500),
    ;

    public final int code;

    Status(int code) {
        this.code = code;
    }

    public static Status byCode(int code) {
        for (Status s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

}
