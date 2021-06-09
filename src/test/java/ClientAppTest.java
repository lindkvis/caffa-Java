import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.caffa.rpc.GrpcClientApp;
import org.caffa.rpc.Object;

class ClientAppTest {
    private GrpcClientApp testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 55555);
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

    @Test
    void document() {
        Object object = testApp.document("");
        String classKeyword = object.getClassKeyword();
        assumeTrue(!classKeyword.isEmpty());
        System.out.println("Main Document Class Keyword: " + classKeyword);

        String json = object.getJson();
        assumeTrue(!json.isEmpty());
        System.out.println("Main Document JSON: " + json);
    }

}