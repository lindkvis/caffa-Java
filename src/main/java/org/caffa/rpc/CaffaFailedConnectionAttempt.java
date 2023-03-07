package org.caffa.rpc;

/**
 * A special exception which clients may decide to ignore and retry
 */
public class CaffaFailedConnectionAttempt extends java.lang.Exception {
    public CaffaFailedConnectionAttempt(String message) {
        super(message);
    }
}
