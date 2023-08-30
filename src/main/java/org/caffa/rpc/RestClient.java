//##################################################################################################
//
//   Caffa
//   Copyright (C) 2023- Kontur AS
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

import java.net.Authenticator;
import java.util.concurrent.TimeUnit;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import javax.net.ssl.SSLContext;

import com.google.gson.GsonBuilder;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.PropertyConfigurator;
import org.caffa.rpc.CaffaConnectionError.FailureType;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class RestClient {
    public enum Status {
        AVAILABLE,
        BUSY_BUT_AVAILABLE,
        BUSY,
        INCOMPATIBLE,
        UNREACHABLE
    }

    private String hostname;
    private int port;

    private PropertyChangeSupport propertyChangeSupport;

    private CaffaSession session = null;
    private SSLContext sslContext = null;
    private String protocolTag = "http://";
    private HttpClient httpClient = null;
    private String username = "";
    private String password = "";
    private CaffaSession.Type sessionType;

    private final ReentrantLock lock = new ReentrantLock();

    /** Defines intervals and timeouts (milliseconds). */
    static final long KEEPALIVE_INTERVAL = 2000;
    static final long KEEPALIVE_TIMEOUT = 5000;
    static final long STATUS_TIMEOUT = 1000;
    static final long SESSION_TIMEOUT = 5000;
    static final long REQUEST_TIMEOUT = 5000;
    private ScheduledExecutorService executor = null;

    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

    public static Status getStatus(String host, int port, int expectedMajorVersion, int expectedMinorVersion, boolean developmentVersion) {
        try {
            if (!checkCompatibility(host, port, expectedMajorVersion, expectedMinorVersion, developmentVersion)) {
                return Status.INCOMPATIBLE;
            }

            HttpRequest request = HttpRequest
                    .newBuilder(
                            new URI("http://" + host + ":" + port + "/session/ready?type=" + CaffaSession.Type.REGULAR.getValue()))
                    .version(HttpClient.Version.HTTP_2).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject result = JsonParser.parseString(response.body()).getAsJsonObject();
            boolean ready = result.get("ready").getAsBoolean();
            boolean hasOtherSessions = result.get("other_sessions").getAsBoolean();

            if (ready && hasOtherSessions) {
                return Status.BUSY_BUT_AVAILABLE;
            } else if (ready) {
                return Status.AVAILABLE;
            } else {
                return Status.BUSY;
            }
        } catch (Exception e) {
            return Status.UNREACHABLE;
        }
    }

    public static boolean checkCompatibility(String host, int port, int expectedMajorVersion, int expectedMinorVersion, boolean developmentVersion) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI("http://" + host + ":" + port + "/app/info")).version(HttpClient.Version.HTTP_2).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(CaffaAppInfo.class, new CaffaAppInfoAdapter());
            CaffaAppInfo appInfo = builder.create().fromJson(response.body(), CaffaAppInfo.class);

            if (appInfo.majorVersion != expectedMajorVersion || appInfo.minorVersion != expectedMinorVersion) {
                return false;
            }

            boolean serverIsDevelopmentVersion = appInfo.patchVersion >= 50;

            if (developmentVersion != serverIsDevelopmentVersion) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Create a new client app. Will compare version to what the server has and
     * refuse connection on mismatch
     *
     * @param host
     * @param port
     * @param logConfigFilePath
     * @param sessionType
     * @param sslContext
     *
     * @throws Exception
     */
    public RestClient(String host, int port,
            String logConfigFilePath, CaffaSession.Type sessionType, SSLContext sslContext) throws Exception {

        this.hostname = host;
        this.port = port;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.sslContext = sslContext;
        this.sessionType = sessionType;

        if (sslContext != null) {
            this.protocolTag = "https://";
        }

        if (!logConfigFilePath.isEmpty()) {
            setupLogging(logConfigFilePath);
        }        
    }

    /**
     * Create a new client app. Will compare version to what the server has and
     * refuse connection on mismatch
     *
     * @param host
     * @param port
     * @param logConfigFilePath
     *
     * @throws Exception
     */
    public RestClient(String host, int port, String logConfigFilePath) throws Exception {
        this(host, port, logConfigFilePath, CaffaSession.Type.REGULAR, null);
    }

    /**
     * Create a new client app. Will compare version to what the server has and
     * refuse connection on mismatch
     *
     * @param host
     * @param port
     * @throws Exception
     */
    public RestClient(String host, int port) throws Exception {
        this(host, port,"");
    }

    public void connect(String username, String password) throws Exception {
        try {
            this.username = username;
            this.password = password;
            this.httpClient = createHttpClient(username, password);

            this.session = this.createSession(sessionType);
            startKeepAliveTransfer();
        } catch (CaffaConnectionError e) {
            cleanUp();
            throw e;         
        }
    }

    public void cleanUp() {
        try {
            stopKeepAliveTransfer();
            CaffaSession session = getExistingSession();
            if (session != null) {
                destroySession();
            }
        } catch (Exception e) {
            logger.warn("Failed to shut down gracefully: " + e.getMessage());
        }
    }

    public CaffaSession.Type getSessionType() {
        CaffaSession.Type type = CaffaSession.Type.INVALID;

        lock();
        if (this.session != null) {
            type = this.session.getType();
        }
        unlock();

        return type;
    }

    private CaffaSession createSession(CaffaSession.Type type) throws CaffaConnectionError {
        try {
            lock();
            String response = performPutRequest("/session/create?type=" + type.ordinal(), SESSION_TIMEOUT, "");

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(CaffaSession.class, new CaffaSessionAdapter());
            CaffaSession session = builder.create().fromJson(response, CaffaSession.class);
            return session;
        } catch (CaffaConnectionError e) {
            throw e;
        } catch (Exception e) {
            throw new CaffaConnectionError(FailureType.MALFORMED_RESPONSE, e.getMessage());
        } finally {
            unlock();
        }
    }

    private void destroySession() throws CaffaConnectionError {
        try {
            lock();
            if (this.session == null)
                return;
            performDeleteRequest("/session/destroy?session_uuid=" + this.session.getUuid(), SESSION_TIMEOUT, "");
        } catch (CaffaConnectionError e) {
            throw e;
        } finally {
            unlock();
        }
    }

    private CaffaSession getSession() throws CaffaConnectionError {
        CaffaSession existingSession = null;

        try {
            lock();
            existingSession = getExistingSession();
            String response = performPutRequest("/session/check?session_uuid=" + existingSession.getUuid(),
                    SESSION_TIMEOUT, "");
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(CaffaSession.class, new CaffaSessionAdapter());
            CaffaSession checkSession = builder.create().fromJson(response, CaffaSession.class);

            if (!checkSession.getUuid().equals(existingSession.getUuid())) {
                existingSession = null;
            }
        } catch (CaffaConnectionError e) {
            throw e;
        } catch (Exception e) {
            throw new CaffaConnectionError(FailureType.MALFORMED_RESPONSE, "Failed to keep alive old session " + e);
        } finally {
            unlock();
        }

        if (existingSession == null) {
            try {
                lock();
                this.session = createSession(CaffaSession.Type.REGULAR);
                existingSession = this.session;
            } catch (CaffaConnectionError e) {
                throw e;
            } finally {
                unlock();
            }
        }
        return existingSession;
    }

    private CaffaSession getExistingSession() {
        lock();
        CaffaSession existingSession = this.session;
        unlock();
        return existingSession;
    }

    public static void setupNullLogging() {
        org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
        org.apache.log4j.Logger.getRootLogger().addAppender(new org.apache.log4j.varia.NullAppender());
    }

    public static void setupLogging(String logConfigFilePath) {
        File log4jConfigFile = new File(logConfigFilePath);
        if (log4jConfigFile.exists()) {
            PropertyConfigurator.configure(log4jConfigFile.getAbsolutePath());
        } else {
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
        }
    }

    private CaffaAppInfo appInfo() throws CaffaConnectionError {
        String response = performGetRequest("/app/info", STATUS_TIMEOUT);
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(CaffaAppInfo.class, new CaffaAppInfoAdapter());
            CaffaAppInfo appInfo = builder.create().fromJson(response, CaffaAppInfo.class);
            return appInfo;
        } catch (Exception e) {
            throw new CaffaConnectionError(FailureType.MALFORMED_RESPONSE, e.getMessage());
        }
    }

    public String appName() throws CaffaConnectionError {
        CaffaAppInfo appInfo = this.appInfo();
        return appInfo.name;
    }

    public String appVersionString() throws CaffaConnectionError {
        CaffaAppInfo appInfo = this.appInfo();
        StringBuilder sb = new StringBuilder();
        sb.append("v");
        sb.append(appInfo.majorVersion);
        sb.append(".");
        sb.append(appInfo.minorVersion);
        sb.append(".");
        sb.append(appInfo.patchVersion);
        return sb.toString();
    }

    public Integer[] appVersion() throws CaffaConnectionError {
        CaffaAppInfo appInfo = this.appInfo();
        return new Integer[] { appInfo.majorVersion, appInfo.minorVersion, appInfo.patchVersion };
    }

    public CaffaObject document(String documentId) throws Exception {
        CaffaSession session = getSession();
        if (session == null)
            return null;

        String documentData = performGetRequest("/" + documentId + "?skeleton=true&session_uuid=" + session.getUuid(), REQUEST_TIMEOUT);
        JsonElement documentDataJson = JsonParser.parseString(documentData);
        assert documentDataJson.isJsonObject();
        JsonObject documentValueObject = documentDataJson.getAsJsonObject();
        String classKeyword = documentValueObject.get("keyword").getAsString();

        String documentSchema = performGetRequest("/schemas/" + classKeyword + "?session_uuid=" + session.getUuid(), REQUEST_TIMEOUT);
        JsonObject documentSchemaObject = JsonParser.parseString(documentSchema).getAsJsonObject();

        CaffaObject object = new GsonBuilder()
                .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this, documentSchemaObject,
                    false))
                .create()
                .fromJson(documentData, CaffaObject.class);

        return object;
    }

    public JsonObject getObjectSchema(String path) {
        try {
            lock();
            if (this.session == null) {
                throw new CaffaConnectionError(FailureType.SESSION_REFUSED, "No valid session");
            }
         
            if (path.startsWith("#")) {
                path = path.substring(1);
            }

            String documentSchema = performGetRequest(path + "?session_uuid=" + session.getUuid(), REQUEST_TIMEOUT);
            JsonObject documentSchemaObject = JsonParser.parseString(documentSchema).getAsJsonObject();
            return documentSchemaObject;
        } catch (CaffaConnectionError e) {
            logger.error(e.getMessage());
            return null;
        } catch(Exception e) {
            logger.error("Malformed response: " + e.getMessage());
            return null;
        } finally {
            unlock();
        }
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
        if (this.executor != null) {
            this.executor.shutdownNow();
            this.executor.awaitTermination(KEEPALIVE_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    private void sendKeepAliveMessage() {
        try {
            lock();
            if (this.session != null) {
                String response = performPutRequest("/session/keepalive?session_uuid=" + this.session.getUuid(),
                        KEEPALIVE_INTERVAL, "");
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(CaffaSession.class, new CaffaSessionAdapter());
                this.session = builder.create().fromJson(response, CaffaSession.class);
            }
        } catch (Exception e) {
            logger.error("Keepalive failed");
            this.session = null;
            firePropertyChange("status", true, false);
        } finally {
            unlock();
        }
    }

    public String getFieldValue(CaffaAbstractField field) {
        try {
            lock();
            if (this.session == null) {
                throw new CaffaConnectionError(FailureType.SESSION_REFUSED, "No valid session");
            }
            CaffaObject fieldOwner = field.getOwner();
            String fullReply = performGetRequest("/object/uuid/" + fieldOwner.uuid + "/" + field.keyword + "?skeleton=true&session_uuid=" + this.session.getUuid(), REQUEST_TIMEOUT);
            JsonElement value = JsonParser.parseString(fullReply);
            return value.toString();            
        } catch (CaffaConnectionError e) {
            logger.error(e.getMessage());
            return "";
        } catch(Exception e) {
            logger.error("Malformed response: " + e.getMessage());
            return "";
        } finally {
            unlock();
        }
    }

    public void setFieldValue(CaffaAbstractField field, String value) {
        try {
            lock();
            if (this.session == null) {
                throw new CaffaConnectionError(FailureType.SESSION_REFUSED, "No valid session");
            }
            CaffaObject fieldOwner = field.getOwner();
            performPutRequest("/object/uuid/" + fieldOwner.uuid + "/" + field.keyword + "?session_uuid=" + this.session.getUuid(), REQUEST_TIMEOUT, value);
        } catch (CaffaConnectionError e) {
            logger.error(e.getMessage());
        } finally {
            unlock();
        }
    }

    public String execute(CaffaObjectMethod method) {
         try {
            lock();
            if (this.session == null) {
                throw new CaffaConnectionError(FailureType.SESSION_REFUSED, "No valid session");
            }
            CaffaObject self = method.getSelf();
            String response = performPutRequest("/object/uuid/" + self.uuid + "/" + method.keyword + "?session_uuid=" + this.session.getUuid(), REQUEST_TIMEOUT, method.getJson());
            return response;
        } catch (CaffaConnectionError e) {
            logger.error(e.getMessage());
            return "";
        } finally {
            unlock();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Trigger a change in a property.
     *
     * @param propertyName name of the property that changes.
     * @param oldValue     old property value.
     * @param newValue     new property value.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    private HttpClient createHttpClient(String username, String password) throws Exception {
        HttpClient client = null;
        if (!username.isEmpty() && !password.isEmpty()) {
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            };
            if (this.sslContext != null) {
                client = HttpClient.newBuilder().authenticator(authenticator).sslContext(this.sslContext).build();
            } else {
                client = HttpClient.newBuilder().authenticator(authenticator).build();
            }
        } else {
            if (this.sslContext != null) {
                client = HttpClient.newBuilder().sslContext(this.sslContext).build();
            } else {
                client = HttpClient.newBuilder().build();
            }
        }
        return client;
    }

    private String performGetRequest(String path, long timeOutMilliSeconds) throws CaffaConnectionError {
        HttpResponse<String> response = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(this.protocolTag + this.hostname + ":" + this.port + path))
                    .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(timeOutMilliSeconds)).GET().build();                    
            if (this.httpClient == null) {
                throw new CaffaConnectionError(FailureType.CONNECTION_ERROR, "No server connection established. Call connect()");
            }
            response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new CaffaConnectionError(FailureType.CONNECTION_ERROR, e.getMessage());
        }
        if (response.statusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new CaffaConnectionError(FailureType.SESSION_REFUSED,
                    "GET request failed with error: " + response.body());
        } else if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new CaffaConnectionError(FailureType.REQUEST_ERROR,
                    "GET request failed with error: " + response.body());
        }
        return response.body();
    }

    private String performPutRequest(String path, long timeOutMilliSeconds, String body) throws CaffaConnectionError {
        HttpResponse<String> response = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(this.protocolTag + this.hostname + ":" + this.port + path))
                    .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(timeOutMilliSeconds))
                    .PUT(HttpRequest.BodyPublishers.ofString(body)).build();
            if (this.httpClient == null) {
                throw new CaffaConnectionError(FailureType.CONNECTION_ERROR, "No server connection established. Call connect()");
            }
            response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new CaffaConnectionError(FailureType.SESSION_REFUSED,
                        "PUT request failed with error: " + response.body());
            } else if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new CaffaConnectionError(FailureType.REQUEST_ERROR,
                        "PUT request failed with error: " + response.body());
            }
        } catch(CaffaConnectionError e) {
            throw e;
        } catch (Exception e) {
            throw new CaffaConnectionError(FailureType.CONNECTION_ERROR, e.getMessage());
        }

        return response.body();
    }

    private String performDeleteRequest(String path, long timeOutMilliSeconds, String body)
            throws CaffaConnectionError {
        HttpResponse<String> response = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(this.protocolTag + this.hostname + ":" + this.port + path))
                    .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(timeOutMilliSeconds)).DELETE()
                    .build();
            if (this.httpClient == null) {
                throw new CaffaConnectionError(FailureType.CONNECTION_ERROR, "No server connection established. Call connect()");
            }        
            response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new CaffaConnectionError(FailureType.CONNECTION_ERROR, e.getMessage());
        }

        if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new CaffaConnectionError(FailureType.SESSION_REFUSED,
                    "DELETE request failed with error: " + response.body());
        } else if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new CaffaConnectionError(FailureType.REQUEST_ERROR,
                    "DELETE request failed with error: " + response.body());
        }
        return response.body();
    }

}
