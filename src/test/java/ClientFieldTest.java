import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.caffa.rpc.GrpcClientApp;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaAbstractField;
import org.caffa.rpc.CaffaField;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ClientFieldTest {
    private GrpcClientApp testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 55555);
    }

    @Test
    void getDocumentFileName() {
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
}
