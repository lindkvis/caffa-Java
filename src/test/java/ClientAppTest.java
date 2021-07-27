import static org.junit.jupiter.api.Assertions.assertTrue;

import org.caffa.rpc.GrpcClientApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientAppTest {
    private GrpcClientApp testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 55555);
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
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