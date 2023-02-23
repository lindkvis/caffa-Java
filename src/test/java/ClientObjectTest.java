import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObjectMethodResult;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaObjectMethod;
import org.caffa.rpc.GrpcClientApp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    private GrpcClientApp testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 50000, "log4j.properties");
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void document() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document(""));

        String classKeyword = object.classKeyword;
        assertTrue(!classKeyword.isEmpty());
        System.out.println("Main Document Class Keyword: " + classKeyword);

        String uuid = object.uuid;
        System.out.println("uuid: " + uuid);

        assertFalse(uuid.isEmpty());
    }

    @Test
    void dumpDocument() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document(""));
        assertTrue(!object.fields().isEmpty());
        System.out.println(object.dump());
    }

    @Test
    void children() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document(""));

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        assertEquals(3, inheritedField.get().length);

    }

    @Test
    void methods() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document(""));
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
            System.out.println("Found child of class: " + child.classKeyword +
                    " ... checking methods!");
            ArrayList<CaffaObjectMethod> childMethods = child.methods();
            assertEquals(1, childMethods.size());
            CaffaObjectMethod method = childMethods.get(0);
            CaffaField<?> doubleMethodArg = method.field("doubleArgument");
            assertDoesNotThrow(() -> doubleMethodArg.set(99.0, Double.class));
            CaffaField<?> intMethodArg = method.field("intArgument");
            assertDoesNotThrow(() -> intMethodArg.set(41, Integer.class));
            CaffaField<?> stringMethodArg = method.field("stringArgument");
            assertDoesNotThrow(() -> stringMethodArg.set("AnotherValue", String.class));

            CaffaField<?> intArrayMethodArgT = method.field("intArrayArgument");
            assertNotNull(intArrayMethodArgT);

            CaffaField<Integer[]> intArrayMethodArg = method.field("intArrayArgument").cast(
                    Integer[].class);
            Integer[] intArrayValues = { 1, 2, 97 };
            assertDoesNotThrow(() -> intArrayMethodArg.set(intArrayValues));

            assertEquals(99.0, doubleMethodArg.get());
            assertEquals(41, intMethodArg.get());
            assertEquals("AnotherValue", stringMethodArg.get());
            assertTrue(Arrays.equals(intArrayValues, intArrayMethodArg.get()));

            CaffaObjectMethodResult result = assertDoesNotThrow(() -> method.execute());

            boolean status = result.field("status").cast(Boolean.class).get();
            assertTrue(status);

            CaffaField<Double> doubleField = child.field("doubleField",
                    Double.class);
            assertEquals(99.0, doubleField.get());

            CaffaField<Integer[]> arrayField = child.field("proxyIntVector").cast(Integer[].class);
            assertTrue(Arrays.equals(intArrayValues, arrayField.get()));
        }
    }

    @Test
    void specificMethod() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document(""));
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = new ArrayList<CaffaObject>();

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        children.add(demoObject);

        CaffaField<CaffaObject[]> inheritedField = object.field("inheritedDemoObjects", CaffaObject[].class);
        children.addAll(Arrays.asList(inheritedField.get()));

        for (CaffaObject child : children) {
            String methodName = new String("copyObject");
            CaffaObjectMethod copyObjectMethod = child.method(methodName);
            assertNotNull(copyObjectMethod);
            assertDoesNotThrow(() -> copyObjectMethod.setParam("doubleArgument", 97.0,
                    Double.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("intArgument", 43,
                    Integer.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("stringArgument",
                    "TestValue", String.class));
            CaffaObjectMethodResult result = assertDoesNotThrow(() -> copyObjectMethod.execute());

            CaffaField<Boolean> status = result.field("status", Boolean.class);
            assertTrue(status.get());

            CaffaField<Double> doubleField = child.field("doubleField",
                    Double.class);
            assertEquals(97.0, doubleField.get());
            assertEquals(43, child.field("intField").get(Integer.class));
            assertEquals("TestValue", child.field("stringField").get(String.class));
        }
    }

    @Test
    void nonExistentMethod() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document(""));
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
            CaffaObjectMethod copyObjectMethod = child.method(methodName);
            assertNull(copyObjectMethod);
        }
    }
}
