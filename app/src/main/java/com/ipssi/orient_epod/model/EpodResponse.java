package com.ipssi.orient_epod.model;

public class EpodResponse {
    public EpodResponse(String message, String status) {
        this.message = message;
        this.status = status;  //1 = otp , -1 =error
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    private String message;
    private String status;
}
