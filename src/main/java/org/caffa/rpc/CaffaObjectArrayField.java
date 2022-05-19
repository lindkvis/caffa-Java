package org.caffa.rpc;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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

        for (RpcObject object : objectList.getObjectsList())
        {
            String jsonString = object.getJson();
            objects.add(new GsonBuilder()
                    .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.owner.channel,true)).create()
                    .fromJson(jsonString, CaffaObject.class));
        }
        return objects;
    }

    @Override
    public GenericArray createChunk(List<CaffaObject> values)
    {
        RpcObjectList.Builder objectList = RpcObjectList.newBuilder();
        for (CaffaObject object : values)
        {
            RpcObject rpcObject = RpcObject.newBuilder().setJson(object.getJson()).build();
            objectList.addObjects(rpcObject);
        }
        return GenericArray.newBuilder().setObjects(objectList).build();
    }

    @Override
    public JsonArray getJsonArray() {
        RpcObjectList objectList = localArray.getObjects();

        JsonArray objects = new JsonArray();
        for (RpcObject object : objectList.getObjectsList()) {
            String jsonString = object.getJson();
            JsonElement el = JsonParser.parseString(jsonString);
            objects.add(el);
        }

        return objects;
    }

    @Override
    public final List<CaffaObject> children()
    {
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
