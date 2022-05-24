import org.caffa.rpc.CaffaArrayField;
import org.caffa.rpc.CaffaIntArrayField;
import org.caffa.rpc.CaffaObjectMethodResult;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaObjectMethod;
import org.caffa.rpc.GrpcClientApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientObjectTest {
    private GrpcClientApp testApp;

    @BeforeAll
    public static void logSetup() {
        Logger.getGlobal().setLevel(Level.INFO);
    }

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 50000);
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void document() {
        CaffaObject object = testApp.document("");

        String classKeyword = object.classKeyword;
        assertTrue(!classKeyword.isEmpty());
        System.out.println("Main Document Class Keyword: " + classKeyword);

        String uuid = object.uuid;
        System.out.println("uuid: " + uuid);

        assertFalse(uuid.isEmpty());
    }

    @Test
    void dumpDocument() {
        CaffaObject object = testApp.document("");
        assertTrue(!object.fields.isEmpty());
        object.dump();
    }

    @Test
    void children() {
        CaffaObject object = testApp.document("");

        ArrayList<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        for (CaffaObject child : children) {
            System.out.println("Found child of class: " + child.classKeyword);
        }
    }

    @Test
    void methods() {
        CaffaObject object = testApp.document("");
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        for (CaffaObject child : children) {
            System.out.println("Found child of class: " + child.classKeyword + " ... checking methods!");
            ArrayList<CaffaObjectMethod> childMethods = child.methods();
            assertEquals(1, childMethods.size());
            CaffaObjectMethod method = childMethods.get(0);

            System.out.println("Found method!!");
            method.dump();
            CaffaField<?> doubleMethodArg = method.field("doubleArgument");
            assertDoesNotThrow(() -> doubleMethodArg.set(99.0, Double.class));
            CaffaField<?> intMethodArg = method.field("intArgument");
            assertDoesNotThrow(() -> intMethodArg.set(41, Integer.class));
            CaffaField<?> stringMethodArg = method.field("stringArgument");
            assertDoesNotThrow(() -> stringMethodArg.set("AnotherValue", String.class));
            CaffaIntArrayField intArrayMethodArg = method.field("intArrayArgument").cast(CaffaIntArrayField.class,
                    Integer.class);
            ArrayList<Integer> intArrayValues = new ArrayList<>();
            intArrayValues.add(1);
            intArrayValues.add(2);
            intArrayValues.add(97);
            intArrayMethodArg.set(intArrayValues);

            assertEquals(99.0, doubleMethodArg.get());
            assertEquals(41, intMethodArg.get());
            assertEquals("AnotherValue", stringMethodArg.get());
            assertEquals(intArrayValues, intArrayMethodArg.get());

            CaffaObjectMethodResult result = method.execute();

            boolean status = result.field("status").cast(Boolean.class).get();
            assertTrue(status);

            CaffaField<Double> doubleField = child.typedField("doubleField", Double.class);
            assertEquals(99.0, doubleField.get());

            CaffaField arrayField = child.field("proxyIntVector");
            assertEquals(intArrayValues, arrayField.get());
        }
    }

    @Test
    void specificMethod() {
        CaffaObject object = testApp.document("");
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        for (CaffaObject child : children) {
            String methodName = new String("copyObject");
            CaffaObjectMethod copyObjectMethod = child.method(methodName);
            assertNotNull(copyObjectMethod);
            assertDoesNotThrow(() -> copyObjectMethod.setParam("doubleArgument", 97.0, Double.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("intArgument", 43, Integer.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("stringArgument", "TestValue", String.class));
            CaffaObjectMethodResult result = copyObjectMethod.execute();

            CaffaField<Boolean> status = result.typedField("status", Boolean.class);
            assertTrue(status.get());

            CaffaField<Double> doubleField = child.typedField("doubleField", Double.class);
            assertEquals(97.0, doubleField.get());
            assertEquals(43, child.field("intField").get(Integer.class));
            assertEquals("TestValue", child.field("stringField").get(String.class));
        }
    }

    @Test
    void nonExistentMethod() {
        CaffaObject object = testApp.document("");
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        for (CaffaObject child : children) {
            String methodName = new String("copyObject");
            CaffaObjectMethod copyObjectMethod = child.method(methodName);
            assertNotNull(copyObjectMethod);
            // Manipulate class keyword
            copyObjectMethod.classKeyword = "rubbish";
            assertDoesNotThrow(() -> copyObjectMethod.setParam("doubleArgument", 97.0, Double.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("intArgument", 43, Integer.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("stringArgument", "TestValue", String.class));
            CaffaObjectMethodResult result = copyObjectMethod.execute();

            assertNull(result);
        }
    }

    @Test
    void methodWithNonExistentSelf() {
        CaffaObject object = testApp.document("");
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());

        for (CaffaObject child : children) {
            String methodName = new String("copyObject");
            CaffaObjectMethod copyObjectMethod = child.method(methodName);

            // Manipulate child's uuid
            child.uuid = "rubbish";
            assertNotNull(copyObjectMethod);
            assertDoesNotThrow(() -> copyObjectMethod.setParam("doubleArgument", 97.0, Double.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("intArgument", 43, Integer.class));
            assertDoesNotThrow(() -> copyObjectMethod.setParam("stringArgument", "TestValue", String.class));
            CaffaObjectMethodResult result = copyObjectMethod.execute();

            assertNull(result);
        }
    }
}
