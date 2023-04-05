package io.enfuse.plugin

class MessageResponse {

    private boolean isSuccess

    private String message

    MessageResponse(boolean isSuccess, String message) {
        this.isSuccess = isSuccess
        this.message = message
    }

    boolean isSuccess() {
        return isSuccess
    }

    String getMessage() {
        return message
    }
}
