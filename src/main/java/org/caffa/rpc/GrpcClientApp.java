//##################################################################################################
//
//   Caffa
//   Copyright (C) 2021- 3D-Radar
//
//   This library may be used under the terms of either the GNU General Public License or
//   the GNU Lesser General Public License as follows:
//
//   GNU General Public License Usage
//   This library is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.
//
//   This library is distributed in the hope that it will be useful, but WITHOUT ANY
//   WARRANTY; without even the implied warranty of MERCHANTABILITY or
//   FITNESS FOR A PARTICULAR PURPOSE.
//
//   See the GNU General Public License at <<http://www.gnu.org/licenses/gpl.html>>
//   for more details.
//
//   GNU Lesser General Public License Usage
//   This library is free software; you can redistribute it and/or modify
//   it under the terms of the GNU Lesser General Public License as published by
//   the Free Software Foundation; either version 2.1 of the License, or
//   (at your option) any later version.
//
//   This library is distributed in the hope that it will be useful, but WITHOUT ANY
//   WARRANTY; without even the implied warranty of MERCHANTABILITY or
//   FITNESS FOR A PARTICULAR PURPOSE.
//
//   See the GNU Lesser General Public License at <<http://www.gnu.org/licenses/lgpl-2.1.html>>
//   for more details.
//
package org.caffa.rpc;

import java.util.concurrent.TimeUnit;

import com.google.gson.GsonBuilder;

import org.caffa.rpc.AppGrpc.AppBlockingStub;
import org.caffa.rpc.ObjectAccessGrpc.ObjectAccessBlockingStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClientApp {
    private final AppBlockingStub appStub;
    private final ObjectAccessBlockingStub objectStub;
    private final ManagedChannel channel;
    private final String sessionUuid;

    private static Logger logger = LoggerFactory.getLogger(GrpcClientApp.class);

    public GrpcClientApp(String host, int port) throws Exception{
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.appStub = AppGrpc.newBlockingStub(channel);
        this.objectStub = ObjectAccessGrpc.newBlockingStub(channel);

        NullMessage message = NullMessage.getDefaultInstance();
        SessionMessage session = this.appStub.createSession(message);
        this.sessionUuid = session.getUuid();
    }

    public void cleanUp() {
        logger.debug("Destroying session!");
        SessionMessage session = SessionMessage.newBuilder().setUuid(this.sessionUuid).build();
        
        this.appStub.destroySession(session);

        logger.debug("Shutting down channels!");
        try {
            this.channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Failed to shut down gracefully" + e.getMessage());
        }
    }

    public String appName() {
        NullMessage message = NullMessage.getDefaultInstance();
        AppInfoReply appInfo = this.appStub.getAppInfo(message);
        return appInfo.getName();
    }

    public String appVersion() {
        NullMessage message = NullMessage.getDefaultInstance();
        AppInfoReply appInfo = this.appStub.getAppInfo(message);
        StringBuilder sb = new StringBuilder();
        sb.append(appInfo.getMajorVersion());
        sb.append(".");
        sb.append(appInfo.getMinorVersion());
        sb.append(".");
        sb.append(appInfo.getPatchVersion());
        return sb.toString();
    }

    public CaffaObject document(String documentId) {
        SessionMessage session = SessionMessage.newBuilder().setUuid(this.sessionUuid).build();
        DocumentRequest request = DocumentRequest.newBuilder().setDocumentId(documentId).setSession(session).build();
        RpcObject object = this.objectStub.getDocument(request);
        String jsonString = object.getJson();
        return new GsonBuilder()
                .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.channel, true, this.sessionUuid)).create()
                .fromJson(jsonString, CaffaObject.class);
    }

}
