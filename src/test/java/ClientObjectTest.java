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

public class ClientObjectTest {
    private GrpcClientApp testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 55555);
    }

    @Test
    void document() {
        CaffaObject object = testApp.document("");

        String classKeyword = object.classKeyword;
        assertTrue(!classKeyword.isEmpty());
        System.out.println("Main Document Class Keyword: " + classKeyword);

        long address = object.serverAddress;
        System.out.println("Address: " + address);

        assertTrue(address != 0);
    }

    @Test
    void documentFields() {
        CaffaObject object = testApp.document("");
        assertTrue(!object.fields.isEmpty());

        Boolean foundDocumentFileName = false;
        for (Map.Entry<String, CaffaAbstractField> entry : object.fields.entrySet()) {
            CaffaAbstractField field = entry.getValue();
            assertTrue(field.keyword == entry.getKey());
            System.out.println("Found field: '" + entry.getKey() + "' (" + field.type + ")");
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
