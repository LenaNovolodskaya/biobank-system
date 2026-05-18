package ru.healthfamily.biobank.exception;

public class ResearchHasLinkedVisitsException extends RuntimeException {
    private final String message;

    public ResearchHasLinkedVisitsException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
