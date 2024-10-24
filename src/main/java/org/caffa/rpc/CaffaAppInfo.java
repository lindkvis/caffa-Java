package org.caffa.rpc;

public class CaffaAppInfo {
    public final int majorVersion;
    public final int minorVersion;
    public final int patchVersion;
    public final String name;
    public final int type;

    public CaffaAppInfo(String name, int majorVersion, int minorVersion, int patchVersion, int type)
    {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.type = type;
    }
}
