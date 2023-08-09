package org.caffa.rpc;

public class CaffaAppInfo {
    public int majorVersion;
    public int minorVersion;
    public int patchVersion;
    public String name;
    public int type;

    public CaffaAppInfo(String name, int majorVersion, int minorVersion, int patchVersion, int type)
    {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.type = type;
    }
}
