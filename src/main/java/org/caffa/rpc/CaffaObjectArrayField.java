package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import io.grpc.ManagedChannel;

import java.util.ArrayList;
import java.util.List;

public class CaffaObjectArrayField extends CaffaArrayField<CaffaObject> {
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectArrayField.class);

    public CaffaObjectArrayField(CaffaObject owner, String keyword, ArrayList<CaffaObject> value) {
        super(owner, keyword, CaffaObject.class);
        assert value != null;
        try {
            this.set(value);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public CaffaObjectArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaObject.class);
    }

    @Override
    public List<CaffaObject> getChunk(GenericArray reply) {
        RpcObjectList objectList = reply.getObjects();

        ArrayList<CaffaObject> objects = new ArrayList<>();

        for (RpcObject object : objectList.getObjectsList()) {
            String jsonString = object.getJson();
            objects.add(new GsonBuilder()
                    .registerTypeAdapter(CaffaObject.class,
                            new CaffaObjectAdapter(this.channel,
                                    this.owner.sessionUuid()))
                    .create()
                    .fromJson(jsonString, CaffaObject.class));
        }
        return objects;
    }

    @Override
    public GenericArray createChunk(List<CaffaObject> values) {
        RpcObjectList.Builder objectList = RpcObjectList.newBuilder();
        for (CaffaObject object : values) {
            RpcObject rpcObject = RpcObject.newBuilder().setJson(object.getJson()).build();
            objectList.addObjects(rpcObject);
        }
        return GenericArray.newBuilder().setObjects(objectList).build();
    }

    @Override
    public JsonArray getJsonArray() {
        assert localArray != null;

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
    protected List<CaffaObject> getListFromJsonArray(JsonArray jsonArray) {
        ArrayList<CaffaObject> values = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            values.add(new GsonBuilder()
                    .registerTypeAdapter(CaffaObject.class,
                            new CaffaObjectAdapter(this.channel,
                                    this.owner.sessionUuid()))
                    .create()
                    .fromJson(element, CaffaObject.class));
        }
        return values;
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
