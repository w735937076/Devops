package com.drp.notify.dto;

import lombok.Data;
import lombok.Setter;

@Data
public class SendResult {
    private boolean success;
    private String errorMessage;
    private String externalId;

    public static SendResult success() {
        SendResult result = new SendResult();
        result.setSuccess(true);
        return result;
    }

    public static SendResult success(String externalId) {
        SendResult result = new SendResult();
        result.setSuccess(true);
        result.setExternalId(externalId);
        return result;
    }

    public static SendResult fail(String errorMessage) {
        SendResult result = new SendResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
