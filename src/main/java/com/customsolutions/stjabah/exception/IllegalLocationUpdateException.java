package com.customsolutions.stjabah.exception;

public class IllegalLocationUpdateException extends RuntimeException {
    public IllegalLocationUpdateException(Long id){
        super("Incident with ID " + id + " cannot be moved because it is no longer in CREATED status.");
    }
}
