import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.caffa.rpc.GrpcApp;

class AppTest {
    private GrpcApp testApp;

    @BeforeEach
    public void setUp() throws Exception{
        testApp = new GrpcApp("localhost", 55555);
    }

    @Test
    void appInfo() {
        String appName = testApp.appName();
        assumeTrue(!appName.isEmpty());
        System.out.println("Application Name: " + appName);
    }
    @Test
    void appVersion() {
        String appVersion = testApp.appVersion();
        assumeTrue(!appVersion.isEmpty());
        System.out.println("Application Version: " + appVersion);
    }
}