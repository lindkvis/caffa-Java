import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObjectMethodResult;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaObjectMethod;
import org.caffa.rpc.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientObjectTest {
    private RestClient testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new RestClient("localhost", 50000,  "log4j.properties");
        testApp.connect("test", "password");
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void document() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));

        String classKeyword = object.keyword;
        assertTrue(!classKeyword.isEmpty());
        System.out.println("Main Document Class Keyword: " + classKeyword);

        String uuid = object.uuid;
        System.out.println("uuid: " + uuid);

        assertFalse(uuid.isEmpty());
    }

    @Test
    void dumpDocument() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        assertTrue(!object.fields().isEmpty());
        System.out.println(object.dump());
    }

    @Test
    void children() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        assertEquals(3, inheritedField.get().length);

    }

    @Test
    void methods() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = new ArrayList<CaffaObject>();

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        children.add(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        children.addAll(Arrays.asList(inheritedField.get()));

        assertEquals(4, children.size());
        for (CaffaObject child : children) {
            System.out.println("Found child of class: " + child.keyword +
                    " ... checking methods!");


            CaffaField<Double> doubleField = child.field("doubleField", Double.class);
            assertDoesNotThrow(() -> doubleField.set(111.0));
            assertEquals(111.0, doubleField.get());

            ArrayList<CaffaObjectMethod> childMethods = child.methods();
            assertEquals(3, childMethods.size());
            CaffaObjectMethod method = childMethods.get(0);
            CaffaField<?> doubleMethodArg = method.field("doubleValue");
            assertDoesNotThrow(() -> doubleMethodArg.set(99.0, Double.class));
            CaffaField<?> intMethodArg = method.field("intValue");
            assertDoesNotThrow(() -> intMethodArg.set(41L, Long.class));
            CaffaField<?> stringMethodArg = method.field("stringValue");
            assertDoesNotThrow(() -> stringMethodArg.set("AnotherValue", String.class));


            CaffaObjectMethod setIntVectorMethod = child.method("setIntVector");
            CaffaField<?> intArrayMethodArgT = setIntVectorMethod.field("intVector");
            assertNotNull(intArrayMethodArgT);

            CaffaField<Long[]> intArrayMethodArg = intArrayMethodArgT.cast(
                    Long[].class);
            Long[] intArrayValues = { 1L, 2L, 97L };
            assertDoesNotThrow(() -> intArrayMethodArg.set(intArrayValues));

            assertEquals(99.0, doubleMethodArg.get());
            assertEquals(41L, intMethodArg.get());
            assertEquals("AnotherValue", stringMethodArg.get());



            assertTrue(Arrays.equals(intArrayValues, intArrayMethodArg.get()));

            assertDoesNotThrow(() -> method.execute());
            assertDoesNotThrow(() -> setIntVectorMethod.execute());

            assertEquals(99.0, doubleField.get());

            CaffaField<Long[]> arrayField = child.field("proxyIntVector").cast(Long[].class);
            assertTrue(Arrays.equals(intArrayValues, arrayField.get()));
        }
    }

    @Test
    void specificMethod() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = new ArrayList<CaffaObject>();

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        children.add(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        children.addAll(Arrays.asList(inheritedField.get()));

        for (CaffaObject child : children) {
            String methodName = new String("copyValues");
            CaffaObjectMethod copyObjectMethod = child.method(methodName);
            System.out.println(copyObjectMethod.dump());
            assertNotNull(copyObjectMethod);
            assertDoesNotThrow(() -> copyObjectMethod.setParam("doubleValue", 97.0,
                    Double.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("intValue", 43L,
                    Long.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("stringValue",
                    "TestValue", String.class));
            assertDoesNotThrow(() -> copyObjectMethod.execute());

            CaffaField<Double> doubleField = child.field("doubleField",
                    Double.class);
            assertEquals(97.0, doubleField.get());
            assertEquals(43, child.field("intField").get(Long.class));
            assertEquals("TestValue", child.field("stringField").get(String.class));
        }
    }

    @Test
    void nonExistentMethod() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = new ArrayList<CaffaObject>();

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        children.add(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        children.addAll(Arrays.asList(inheritedField.get()));

        assertTrue(!children.isEmpty());
        for (CaffaObject child : children) {
            String methodName = new String("copyObjectDoesNotExist");
            assertThrows( RuntimeException.class, () -> child.method(methodName));
        }
    }
}
