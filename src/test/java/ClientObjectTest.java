import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaObjectMethod;
import org.caffa.rpc.RestClient;

import java.util.Arrays;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClientObjectTest {
    private RestClient testApp;
    private final String hostname = "127.0.0.1";

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new RestClient(hostname, 50000,  "log4j.properties");
        testApp.connect("test", "password");
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void document() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));

        String classKeyword = object.keyword();
        assertFalse(classKeyword.isEmpty());
        System.out.println("Main Document Class Keyword: " + classKeyword);

        String uuid = object.uuid();
        System.out.println("uuid: " + uuid);

        assertFalse(uuid.isEmpty());
    }

    @Test
    void dumpDocument() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        assertFalse(object.fields().isEmpty());
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
        assertFalse(methods.isEmpty());

        ArrayList<CaffaObject> children = new ArrayList<>();

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        children.add(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        children.addAll(Arrays.asList(inheritedField.get()));

        assertEquals(4, children.size());
        for (CaffaObject child : children) {
            System.out.println("Found child of class: " + child.keyword() +
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


            assertArrayEquals(intArrayValues, intArrayMethodArg.get());

            assertDoesNotThrow(() -> method.execute());
            assertDoesNotThrow(() -> setIntVectorMethod.execute());

            assertEquals(99.0, doubleField.get());

            CaffaField<Long[]> arrayField = child.field("proxyIntVector").cast(Long[].class);
            assertArrayEquals(intArrayValues, arrayField.get());
        }
    }

    @Test
    void specificMethod() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertFalse(methods.isEmpty());

        ArrayList<CaffaObject> children = new ArrayList<>();

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        children.add(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        children.addAll(Arrays.asList(inheritedField.get()));

        for (CaffaObject child : children) {
            String methodName = "copyValues";
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
        assertFalse(methods.isEmpty());

        ArrayList<CaffaObject> children = new ArrayList<>();

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        children.add(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        children.addAll(Arrays.asList(inheritedField.get()));

        assertFalse(children.isEmpty());
        for (CaffaObject child : children) {
            String methodName = "copyObjectDoesNotExist";
            assertThrows( RuntimeException.class, () -> child.method(methodName));
        }
    }
}
