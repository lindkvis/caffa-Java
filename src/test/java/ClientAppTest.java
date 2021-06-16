import static org.junit.jupiter.api.Assertions.assertTrue;

import org.caffa.rpc.GrpcClientApp;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaAbstractField;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class ClientAppTest {
    private GrpcClientApp testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 55555);
    }

    @Test
    void appInfo() {
        String appName = testApp.appName();
        assertTrue(!appName.isEmpty());
        System.out.println("Application Name: " + appName);
    }

    @Test
    void appVersion() {
        String appVersion = testApp.appVersion();
        assertTrue(!appVersion.isEmpty());
        System.out.println("Application Version: " + appVersion);
    }

}