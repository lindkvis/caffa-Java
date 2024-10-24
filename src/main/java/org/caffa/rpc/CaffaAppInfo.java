package org.caffa.rpc;

public record CaffaAppInfo(String name, int majorVersion, int minorVersion, int patchVersion, int type) {
}
