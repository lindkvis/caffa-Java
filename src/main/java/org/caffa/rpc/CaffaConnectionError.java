package org.caffa.rpc;

/**
 * A special and fatal connection error
 */
public class CaffaConnectionError extends java.lang.Exception {
    public enum FailureType
    {
        VERSION_MISMATCH,
        SESSION_REFUSED, // Server refuses to accept the session
        CONNECTION_ERROR, // Failed to connect
        REQUEST_ERROR, // The request fails on the server
        MALFORMED_RESPONSE, // The response is not valid JSON
        LOST_CONTROL
    }
    public FailureType type;

    public CaffaConnectionError(FailureType type, String message) {        
        super(message);

        this.type = type;
    }
}
