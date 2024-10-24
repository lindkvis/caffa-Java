package org.caffa.rpc;

public class CaffaSession {
    public enum Type {
        INVALID,
        REGULAR,
        OBSERVING;

        public static Type fromInt(int type) {
            return switch (type) {
                case 0 -> INVALID;
                case 1 -> REGULAR;
                case 2 -> OBSERVING;
                default -> throw new RuntimeException("Cannot convert integer " + type + " to session type");
            };
        }

        public static Type fromString(String type) {
            if (type.equals(INVALID.name())) {
                return INVALID;
            } else if (type.equals(REGULAR.name())) {
                return REGULAR;
            } else if (type.equals(OBSERVING.name())) {
                return OBSERVING;
            }
            throw new RuntimeException("Cannot convert type " + type + " to session type");
        }

        public int getValue() {
            return this.ordinal();
        }
    }

    private final String uuid;
    private final Type type;

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
