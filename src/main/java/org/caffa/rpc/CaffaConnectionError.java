package org.caffa.rpc;

/**
 * A special and fatal connection error
 */
public class CaffaConnectionError extends java.lang.Exception {
    public enum FailureType
    {
        VERSION_MISMATCH,
        SESSION_REFUSED,
        CONNECTION_ERROR,
        LOST_CONTROL
    }
    public FailureType type;

    public CaffaConnectionError(FailureType type, String message) {        
        super(message);

        this.type = type;
    }
}
