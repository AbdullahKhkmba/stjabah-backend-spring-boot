package com.customsolutions.stjabah.exception;

public class InvalidStatusForDispatchException extends RuntimeException {
    public InvalidStatusForDispatchException(Long id)
    {
        super("Incident incident with id: " + id + " is already dispatched");
    }
}
