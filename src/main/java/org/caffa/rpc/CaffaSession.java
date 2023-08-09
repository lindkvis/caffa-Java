package org.caffa.rpc;

public class CaffaSession {
    public enum Type {
        INVALID,
        REGULAR,
        OBSERVING;

        public static Type fromInt(int type) {
            switch (type) {
                case 0:
                    return INVALID;
                case 1:
                    return REGULAR;
                case 2:
                    return OBSERVING;
                default:
                    throw new RuntimeException("Cannot convert integer " + type + " to session type");
            }
        }

        public int getValue() {
            return this.ordinal();
        }
    }

    private String uuid;
    private Type type;

    CaffaSession(String uuid, Type type) {
        this.uuid = uuid;
        this.type = type;
    }

    public String getUuid() {
        return this.uuid;
    }

    public Type getType() {
        return this.type;
    }
}
