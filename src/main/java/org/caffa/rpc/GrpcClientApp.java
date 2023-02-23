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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.google.gson.GsonBuilder;

import org.caffa.rpc.AppGrpc.AppBlockingStub;
import org.caffa.rpc.ObjectAccessGrpc.ObjectAccessBlockingStub;
import org.caffa.rpc.SessionParameters;
import org.caffa.rpc.SessionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.PropertyConfigurator;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClientApp {
    public enum Status {
        AVAILABLE,
        BUSY_BUT_AVAILABLE,
        BUSY,
        INCOMPATIBLE,
        UNREACHABLE
    }

    private final AppBlockingStub appStub;
    private final ObjectAccessBlockingStub objectStub;
    private final ManagedChannel channel;
    private SessionMessage session = null;
    private final ReentrantLock lock = new ReentrantLock();

    /** Defines intervals and timeouts (milliseconds). */
    static final long KEEPALIVE_INTERVAL = 500;
    static final long KEEPALIVE_TIMEOUT = 5000;
    static final long STATUS_TIMEOUT = 300;
    static final long SESSION_TIMEOUT = 1000;
    private ScheduledExecutorService executor;

    private static Logger logger = LoggerFactory.getLogger(GrpcClientApp.class);

    public static Status getStatus(String host, int port, int expectedMajorVersion, int expectedMinorVersion) {
        try {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
            AppBlockingStub appStub = AppGrpc.newBlockingStub(channel);

            NullMessage nullMessage = NullMessage.getDefaultInstance();
            AppInfoReply appInfo = appStub.withDeadlineAfter(STATUS_TIMEOUT, TimeUnit.MILLISECONDS)
                    .getAppInfo(nullMessage);
            if (appInfo.getMajorVersion() != expectedMajorVersion
                    || appInfo.getMinorVersion() != expectedMinorVersion) {
                return Status.INCOMPATIBLE;
            }

            SessionParameters parameters = SessionParameters.newBuilder().setType(SessionType.REGULAR).build();
            ReadyMessage ready = appStub.withDeadlineAfter(STATUS_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readyForSession(parameters);

            if (ready.getReady() && ready.getHasOtherSessions()) {
                return Status.BUSY_BUT_AVAILABLE;
            } else if (ready.getReady()) {
                return Status.AVAILABLE;
            } else {
                return Status.BUSY;
            }
        } catch (Exception e) {
            return Status.UNREACHABLE;
        }
    }

    public GrpcClientApp(String host, int port, String logConfigFilePath, SessionType sessionType) throws Exception {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.appStub = AppGrpc.newBlockingStub(channel);
        this.objectStub = ObjectAccessGrpc.newBlockingStub(channel);

        if (!logConfigFilePath.isEmpty()) {
            setupLogging(logConfigFilePath);
        }

        this.session = createSession(sessionType);
        if (this.session == null) {
            throw new RuntimeException("Failed to create session");
        }

        startKeepAliveTransfer();
    }

    public GrpcClientApp(String host, int port, String logConfigFilePath) throws Exception {
        this(host, port, logConfigFilePath, SessionType.REGULAR);
    }

    public GrpcClientApp(String host, int port) throws Exception {
        this(host, port, "");
    }

    public SessionType getSessionType() {
        SessionType type = SessionType.INVALID;

        lock();
        if (this.session != null) {
            type = this.session.getType();
        }
        unlock();

        return type;
    }

    private SessionMessage createSession(SessionType type) throws Exception {
        try {
            SessionParameters parameters = SessionParameters.newBuilder().setType(type).build();
            SessionMessage session = this.appStub.withDeadlineAfter(SESSION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .createSession(parameters);
            return session;
        } catch (Exception e) {
            logger.error("Failed to create new session: ", e);
            throw e;
        }
    }

    private SessionMessage getSession() throws Exception {
        SessionMessage existingSession = null;
        lock();
        existingSession = this.session;
        unlock();

        if (existingSession != null) {
            try {
                SessionMessage checkSession = this.appStub.withDeadlineAfter(KEEPALIVE_TIMEOUT, TimeUnit.MILLISECONDS)
                        .checkSession(existingSession);
                if (!checkSession.getUuid().equals(existingSession.getUuid())) {
                    throw new RuntimeException("Session UUID mismatch");
                }
            } catch (Exception e) {
                existingSession = null;
                logger.warn("Could not keep alive old session: " + e.getMessage());
            }
        }

        if (existingSession == null) {
            lock();
            this.session = createSession(SessionType.REGULAR);
            existingSession = this.session;
            unlock();
        }

        return existingSession;
    }

    public static void setupLogging(String logConfigFilePath) {
        File log4jConfigFile = new File(logConfigFilePath);
        if (log4jConfigFile.exists()) {
            System.out.println("Reading log file: " + log4jConfigFile.getAbsolutePath());

            PropertyConfigurator.configure(log4jConfigFile.getAbsolutePath());
        } else {
            System.out.println("Writing log file: " + log4jConfigFile.getAbsolutePath());
            // CSOFF: Empty Block
            try {
                FileWriter writer = new FileWriter(log4jConfigFile);
                String logFile = (log4jConfigFile.getParent() + File.separator).replace("\\", "\\\\")
                        + "log4j.log";
                PrintWriter pw = new PrintWriter(writer);
                pw.println("log4j.rootLogger=INFO, R");
                pw.println();
                pw.println("log4j.appender.R=org.apache.log4j.RollingFileAppender");
                pw.println(String.format("log4j.appender.R.File=%s", logFile));
                pw.println("log4j.appender.R.layout=org.apache.log4j.PatternLayout");
                pw.println(
                        "log4j.appender.R.layout.ConversionPattern=%d{ISO8601} [%t] %-5p %c %x - %m%n");
                pw.println("log4j.appender.R.MaxFileSize=10MB");
                pw.println("log4j.appender.R.MaxBackupIndex=5");
                pw.close();
                writer.close();
                PropertyConfigurator.configure(log4jConfigFile.getAbsolutePath());
            } catch (Exception ex) {
                // Do nothing
            }
            // CSON: Empty Block
        }
    }

    public void cleanUp() {
        try {
            stopKeepAliveTransfer();
            SessionMessage session = getSession();
            if (session != null) {
                logger.debug("Destroying session!");
                this.appStub.withDeadlineAfter(SESSION_TIMEOUT, TimeUnit.MILLISECONDS).destroySession(session);

                logger.debug("Shutting down channels!");
                this.channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.warn("Failed to shut down gracefully" + e.getMessage());
        }
    }

    public String appName() {
        NullMessage message = NullMessage.getDefaultInstance();
        AppInfoReply appInfo = this.appStub.withDeadlineAfter(SESSION_TIMEOUT, TimeUnit.MILLISECONDS)
                .getAppInfo(message);
        return appInfo.getName();
    }

    public String appVersionString() {
        NullMessage message = NullMessage.getDefaultInstance();
        AppInfoReply appInfo = this.appStub.withDeadlineAfter(SESSION_TIMEOUT, TimeUnit.MILLISECONDS)
                .getAppInfo(message);
        StringBuilder sb = new StringBuilder();
        sb.append("v");
        sb.append(appInfo.getMajorVersion());
        sb.append(".");
        sb.append(appInfo.getMinorVersion());
        sb.append(".");
        sb.append(appInfo.getPatchVersion());
        return sb.toString();
    }

    public Integer[] appVersion() {
        NullMessage message = NullMessage.getDefaultInstance();
        AppInfoReply appInfo = this.appStub.withDeadlineAfter(SESSION_TIMEOUT, TimeUnit.MILLISECONDS)
                .getAppInfo(message);

        return new Integer[] { appInfo.getMajorVersion(), appInfo.getMinorVersion(), appInfo.getPatchVersion() };
    }

    public CaffaObject document(String documentId) throws Exception {
        SessionMessage session = getSession();
        if (session == null)
            return null;

        DocumentRequest request = DocumentRequest.newBuilder().setDocumentId(documentId).setSession(session).build();

        RpcObject object = this.objectStub.getDocument(request);
        String jsonString = object.getJson();
        return new GsonBuilder()
                .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.channel, session.getUuid()))
                .create()
                .fromJson(jsonString, CaffaObject.class);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    private void startKeepAliveTransfer() throws Exception {
        try {
            this.executor = Executors.newSingleThreadScheduledExecutor();
            this.executor.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            sendKeepAliveMessage();
                        }
                    },
                    KEEPALIVE_INTERVAL,
                    KEEPALIVE_INTERVAL,
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Failed to start keepalive: ", e);
            throw e;
        }
    }

    private void stopKeepAliveTransfer() throws InterruptedException {
        this.executor.shutdownNow();
        this.executor.awaitTermination(KEEPALIVE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void sendKeepAliveMessage() {
        lock();
        if (this.session != null) {
            try {
                SessionMessage response = this.appStub.withDeadlineAfter(KEEPALIVE_TIMEOUT, TimeUnit.MILLISECONDS)
                        .keepSessionAlive(this.session);
                this.session = response;
            } catch (Exception e) {
                logger.error("Keepalive failed");
                this.session = null;
            }
        }
        unlock();
    }

}
