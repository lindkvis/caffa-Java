import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.caffa.rpc.GrpcClientApp;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaObjectMethod;
import org.caffa.rpc.CaffaAbstractField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Set;

public class ClientObjectTest {
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
            assertTrue(!childMethods.isEmpty());
            for (CaffaObjectMethod method : childMethods)
            {
                System.out.println("Found method!!");
                method.dump();
            }
        }
    }
}
