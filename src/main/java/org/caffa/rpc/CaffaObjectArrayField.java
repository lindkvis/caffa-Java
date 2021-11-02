package org.caffa.rpc;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class CaffaObjectArrayField extends CaffaArrayField<CaffaObject> {

    public CaffaObjectArrayField(CaffaObject owner, String keyword, ArrayList<CaffaObject> value) {
        super(owner, keyword, CaffaObject.class);
        createAccessor(false);
        this.set(value);
    }

    public CaffaObjectArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaObject.class);
        createAccessor(true);
    }

    @Override
    public List<CaffaObject> getChunk(GenericArray reply) {
        RpcObjectList objectList = reply.getObjects();

        ArrayList<CaffaObject> objects = new ArrayList<>();

        for (RpcObject object : objectList.getObjectsList()) {
            String jsonString = object.getJson();
            System.out.println("Got child JSON: " + jsonString);
            objects.add(
                    new GsonBuilder().registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.owner.channel))
                            .create().fromJson(jsonString, CaffaObject.class));
        }
        return objects;
    }

    @Override
    public GenericArray createChunk(List<CaffaObject> values) {
        org.caffa.rpc.RpcObjectList.Builder objectListBuilder = RpcObjectList.newBuilder();
        for (CaffaObject caffaObject : values) {
            objectListBuilder.addObjects(RpcObject.newBuilder().setJson(caffaObject.getJson()));
        }
        return GenericArray.newBuilder().setObjects(objectListBuilder).build();
    }

    @Override
    public final List<CaffaObject> children() {
        return get();
    }

    @Override
    public void dump() {
        System.out.println("CaffaObjectArrayField {");
        super.dump();
        for (CaffaObject object : this.get()) {
            object.dump();
        }
        System.out.println("}");
    }

    @Override
    public CaffaField<ArrayList<CaffaObject>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaObjectArrayField(owner, keyword);
    }
}
