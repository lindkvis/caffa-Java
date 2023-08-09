import static org.junit.jupiter.api.Assertions.assertTrue;

import org.caffa.rpc.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ClientAppTest {
    private RestClient testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new RestClient("localhost", 50000, -1, -1, "log4j.properties");
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void appInfo() {
        String appName = assertDoesNotThrow(() ->testApp.appName());
        assertTrue(!appName.isEmpty());
    }

    @Test
    void appVersionString() {
        String appVersionString = assertDoesNotThrow(() ->testApp.appVersionString());
        assertTrue(!appVersionString.isEmpty());
    }

}