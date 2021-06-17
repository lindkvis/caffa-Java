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

import org.caffa.rpc.AppInfo;
import org.caffa.rpc.AppGrpc;
import org.caffa.rpc.AppGrpc.AppBlockingStub;
import org.caffa.rpc.DocumentRequest;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaFieldAdapter;
import org.caffa.rpc.ObjectAccessGrpc;
import org.caffa.rpc.ObjectAccessGrpc.ObjectAccessBlockingStub;
import org.caffa.rpc.Object;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import com.google.protobuf.Empty;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

public class GrpcClientApp {
    private final AppBlockingStub appStub;
    private final ObjectAccessBlockingStub objectStub;
    private final ManagedChannel channel;

    public GrpcClientApp(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.appStub = AppGrpc.newBlockingStub(channel);
        this.objectStub = ObjectAccessGrpc.newBlockingStub(channel);
    }

    public void cleanUp() {
        System.out.println("Shutting down channels!");
        try {
            this.channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Failed to shut down gracefully" + e.getMessage());
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
        DocumentRequest request = DocumentRequest.newBuilder().setDocumentId(documentId).build();
        Object object = this.objectStub.getDocument(request);
        String jsonString = object.getJson();
        CaffaObject caffaObject = new GsonBuilder()
                .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.channel)).create()
                .fromJson(jsonString, CaffaObject.class);
        return caffaObject;
    }

}
