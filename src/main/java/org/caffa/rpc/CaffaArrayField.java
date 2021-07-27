package org.caffa.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.grpc.StatusRuntimeException;

public abstract class CaffaArrayField<DataType> extends CaffaAbstractField {
    private final Class<DataType> dataType;

    public CaffaArrayField(CaffaObject owner, String keyword, Class<DataType> dataType) {
        super(owner, keyword);
        this.dataType = dataType;
    }

    public ArrayList<DataType> get() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.owner.channel));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this.owner);
        Object self = Object.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(self).build();

        ArrayList<DataType> values = new ArrayList<DataType>();

        try {
            Iterator<GetterArrayReply> replies = this.fieldStub.getArrayValue(fieldRequest);
            while (replies.hasNext()) {
                GetterArrayReply reply = replies.next();
                List<DataType> chunk = getChunk(reply);
                values.addAll(chunk);
            }

        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
        return values;
    }

    abstract protected List<DataType> getChunk(GetterArrayReply reply);

    public void dump() {
        System.out.println("CaffaArrayField <" + dataType + "> {");
        super.dump();
        System.out.println("}");
    }

    public abstract CaffaAbstractField newInstance(CaffaObject owner, String keyword);

    public Class<?> type() {
        return this.dataType;
    }
}
