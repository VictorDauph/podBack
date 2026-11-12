package com.pod.back.printful.exception;

public class PrintfulApiException extends RuntimeException {

    private final int httpStatus;
    private final Integer printfulCode;
    private final String printfulReason;

    public PrintfulApiException(int httpStatus, Integer printfulCode, String printfulReason) {
        super("Erreur API Printful (" + httpStatus + "): " + printfulReason);
        this.httpStatus = httpStatus;
        this.printfulCode = printfulCode;
        this.printfulReason = printfulReason;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Integer getPrintfulCode() {
        return printfulCode;
    }

    public String getPrintfulReason() {
        return printfulReason;
    }
}
