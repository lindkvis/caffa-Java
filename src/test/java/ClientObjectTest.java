import org.caffa.rpc.CaffaObjectMethodResult;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static void logSetup()
    {
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
        for (CaffaObject child : children){
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
            stringMethodArg.set("AnotherValue", String.class);
            assertEquals(99.0, doubleMethodArg.get());
            assertEquals(41, intMethodArg.get());
            assertEquals("AnotherValue", stringMethodArg.get());
            
            CaffaObjectMethodResult result = method.execute();

            boolean status = result.field("status").cast(Boolean.class).get();
            assertTrue(status);

            CaffaField<Double> doubleField = child.typedField("doubleMember", Double.class);
            assertEquals(99.0, doubleField.get());
        }
    }

    @Test
    void specificMethod() {
        CaffaObject object = testApp.document("");
        ArrayList<CaffaObjectMethod> methods = object.methods();
        assertTrue(methods.isEmpty());

        ArrayList<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        for (CaffaObject child : children){
            String methodName = new String("copyObject");
            CaffaObjectMethod copyObjectMethod = child.method(methodName);
            assertNotNull(copyObjectMethod);
            copyObjectMethod.setParam("doubleArgument", 97.0, Double.class);
            copyObjectMethod.setParam("intArgument", 43, Integer.class);
            copyObjectMethod.setParam("stringArgument", "TestValue", String.class);
            copyObjectMethod.execute();

            CaffaField<Double> doubleField = child.typedField("doubleMember", Double.class);
            assertEquals(97.0, doubleField.get());
            assertEquals(43, child.field("intMember").get(Integer.class));
            assertEquals("TestValue", child.field("stringMember").get(String.class));
        }
    }
}
