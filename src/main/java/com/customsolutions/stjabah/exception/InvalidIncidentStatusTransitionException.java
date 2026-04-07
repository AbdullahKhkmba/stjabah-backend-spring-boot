package com.customsolutions.stjabah.exception;

import com.customsolutions.stjabah.entity.IncidentStatus;

public class InvalidIncidentStatusTransitionException extends RuntimeException {
    public InvalidIncidentStatusTransitionException(IncidentStatus from, IncidentStatus to) {
        super("Cannot transition incident from " + from + " to " + to);
    }
}