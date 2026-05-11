package com.tfg.ucm.dbcase.dto;

public record DataType(Domain domain, int length) {

    public static final int NO_LENGTH = 999;

    public static DataType of(Domain domain) {
        return new DataType(domain, NO_LENGTH);
    }

    public static DataType of(Domain domain, int length) {
        return new DataType(domain, length);
    }

    public boolean hasLength() {
        return length != NO_LENGTH;
    }

    @Override
    public String toString() {
        return hasLength() ? domain + "(" + length + ")" : domain.toString();
    }
}
