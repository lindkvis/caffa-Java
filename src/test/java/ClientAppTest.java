import org.caffa.rpc.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientAppTest {
    private RestClient testApp;
    private final String hostname = "127.0.0.1";

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new RestClient(hostname, Integer.parseInt(System.getProperty("port", "50000")), "log4j.properties");
        testApp.connect("test", "password");
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void appInfo() {
        String appName = assertDoesNotThrow(() ->testApp.appName());
        assertFalse(appName.isEmpty());
    }

    @Test
    void appVersionString() {
        String appVersionString = assertDoesNotThrow(() ->testApp.appVersionString());
        assertFalse(appVersionString.isEmpty());
    }

}