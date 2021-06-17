import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.caffa.rpc.GrpcClientApp;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaAbstractField;
import org.caffa.rpc.CaffaArrayField;
import org.caffa.rpc.CaffaField;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ClientFieldTest {
    private GrpcClientApp testApp;

    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 55555);
    }

    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void getDocumentFileName() {
        GrpcClientApp testApp = new GrpcClientApp("localhost", 55555);
        {
            CaffaObject object = testApp.document("");
            assertTrue(!object.fields.isEmpty());

            object.dump();

            String key = "DocumentFileName";
            assertTrue(object.fields.containsKey(key));
            CaffaAbstractField field = object.fields.get(key);
            assertTrue(field != null);
            assertEquals(key, field.keyword);

            CaffaField<String> fileNameField = (CaffaField<String>) field;
            String originalValue = fileNameField.get();
            assertEquals("dummyFileName", originalValue);
            fileNameField.set("TestValue");
            String value = fileNameField.get();
            assertEquals("TestValue", value);
            fileNameField.set(originalValue);
            assertEquals(originalValue, fileNameField.get());
        }
        testApp.cleanUp();

    }

    @Test
    void documentFields() {
        GrpcClientApp testApp = new GrpcClientApp("localhost", 55555);
        {
            CaffaObject object = testApp.document("");
            assertTrue(!object.fields.isEmpty());

            Boolean foundDocumentFileName = false;
            for (Map.Entry<String, CaffaAbstractField> entry : object.fields.entrySet()) {
                CaffaAbstractField field = entry.getValue();
                assertTrue(field.keyword.equals(entry.getKey()));
                System.out.println("Found field: '" + entry.getKey() + "' (" + field.getType() + ")");
                if (field.keyword.equals("DocumentFileName")) {
                    foundDocumentFileName = true;
                }
            }
            assertTrue(foundDocumentFileName);
        }
        testApp.cleanUp();

    }

    @Test
    void floatVector() {
        GrpcClientApp testApp = new GrpcClientApp("localhost", 55555);
        {
            CaffaObject object = testApp.document("");

            ArrayList<CaffaObject> children = object.children();
            assertTrue(!children.isEmpty());
            CaffaObject demoObject = children.get(0);
            System.out.println("Check which field was actually created:");
            demoObject.field("floatVector").dump();
            CaffaArrayField<Float> floatVector = (CaffaArrayField<Float>) demoObject.field("floatVector");
            ArrayList<Float> values = floatVector.get();
            assertTrue(!values.isEmpty());

            System.out.print("Printing first ten floats: ");
            for (int i = 0; i < 10; ++i) {
                System.out.print(values.get(i) + " ");
            }
            System.out.print("\n");
        }
        testApp.cleanUp();
    }
}
