import static org.junit.jupiter.api.Assertions.assertTrue;

import org.caffa.rpc.GrpcClientApp;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaAbstractField;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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

    @Test
    void document() {
        CaffaObject object = testApp.document("");

        String classKeyword = object.classKeyword;
        assertTrue(!classKeyword.isEmpty());
        System.out.println("Main Document Class Keyword: " + classKeyword);

        long address = object.address;
        System.out.println("Address: " + address);

        assertTrue(address != 0);
    }

    @Test
    void documentFields() {
        CaffaObject object = testApp.document("");
        assertTrue(!object.fields.isEmpty());

        Boolean foundDocumentFileName = false;
        for (CaffaAbstractField field : object.fields) {
            System.out.println("Found field: '" + field.keyword + "' (" + field.type + ")");
            if (field.keyword.equals("DocumentFileName")) {
                foundDocumentFileName = true;
            }
        }
        assertTrue(foundDocumentFileName);
    }

    @Test
    void dumpDocumentFields() {
        CaffaObject object = testApp.document("");
        assertTrue(!object.fields.isEmpty());
        object.dump();
    }
}