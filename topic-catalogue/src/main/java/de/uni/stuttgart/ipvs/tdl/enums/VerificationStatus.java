package de.uni.stuttgart.ipvs.tdl.enums;

public enum VerificationStatus {

    VALID("valid"),
    INVALID("invalid"),
    IN_PROGRESS("in progress"),
    UNKNOWN("unknown");

    private final String text;

    VerificationStatus(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
