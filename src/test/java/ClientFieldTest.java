import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.caffa.rpc.CaffaAbstractField;
import org.caffa.rpc.CaffaArrayField;
import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaFloatArrayField;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.GrpcClientApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientFieldTest {
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
    void getDocumentFileName() {
        CaffaObject object = testApp.document("");
        assertTrue(!object.fields.isEmpty());

        object.dump();

        String key = "DocumentFileName";
        assertTrue(object.fields.containsKey(key));
        CaffaAbstractField field = object.fields.get(key);
        assertNotNull(field);
        assertEquals(key, field.keyword);

        CaffaField<String> fileNameField = field.cast();
        String originalValue = fileNameField.get();
        assertEquals("dummyFileName", originalValue);
        fileNameField.set("TestValue");
        String value = fileNameField.get();
        assertEquals("TestValue", value);
        fileNameField.set(originalValue);
        assertEquals(originalValue, fileNameField.get());
    }
    @Test
    void documentFields() {
        CaffaObject object = testApp.document("");
        assertTrue(!object.fields.isEmpty());

        Boolean foundDocumentFileName = false;
        for (Map.Entry<String, CaffaAbstractField> entry : object.fields.entrySet()) {
            CaffaAbstractField field = entry.getValue();
            assertEquals(field.keyword, entry.getKey());
            System.out.println("Found field: '" + entry.getKey() + "' (" + field.type() + ")");
            if (field.keyword.equals("DocumentFileName")) {
                foundDocumentFileName = true;
            }
        }
        assertTrue(foundDocumentFileName);
    }

    @Test
    void floatVector() {
        CaffaObject object = testApp.document("");

        List<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        CaffaObject demoObject = children.get(0);
        System.out.println("Check which field was actually created:");
        demoObject.<CaffaFloatArrayField>field("floatVector").dump();
        CaffaArrayField<Float> floatVector = demoObject.field("floatVector");
        List<Float> values = floatVector.get();
        assertTrue(!values.isEmpty());

        System.out.print("Printing first ten floats: ");
        for (int i = 0; i < 10; ++i) {
            System.out.print(values.get(i) + " ");
        }
        System.out.print("\n");
    }

    @Test
    void doubleMember() {
        CaffaObject object = testApp.document("");

        List<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        CaffaObject demoObject = children.get(0);
        System.out.println("Check that a double field was actually created:");
        demoObject.<CaffaField<Double>>field("doubleMember").dump();
        CaffaField<Double> doubleField = demoObject.field("doubleMember");
        Double originalValue = doubleField.get();
        System.out.println("Original double value: " + originalValue);
        Double newValue = 45.3;
        doubleField.set(newValue);
        System.out.println("Setting double value: " + newValue);

        assertEquals(newValue, doubleField.get());
        System.out.println("Confirmed values match!");

        doubleField.set(originalValue);
        assertEquals(originalValue, doubleField.get());
    }
}
