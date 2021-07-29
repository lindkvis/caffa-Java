package org.caffa.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.grpc.StatusRuntimeException;

public abstract class CaffaArrayField<T> extends CaffaAbstractField {
    private final Class<T> dataType;
    private FieldAccessGrpc.FieldAccessBlockingStub fieldStub = null;
    private GenericArray localArray = null;

    protected CaffaArrayField(CaffaObject owner, String keyword, Class<T> dataType) {
        super(owner, keyword);
        this.dataType = dataType;

    }

    public void createAccessor(boolean grpc)
    {
        if (grpc)
        {
            if (this.owner != null) {
                this.fieldStub = FieldAccessGrpc.newBlockingStub(this.owner.channel);
            }    
        }
        else{
            localArray = GenericArray.getDefaultInstance();
        }
    }

    public List<T> get() {
        logger.log(Level.INFO, "Sending get request");

        if (this.localArray != null)
        {
            return getChunk(this.localArray);
        }
        
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.owner.channel));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this.owner);
        Object self = Object.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(self).build();

        ArrayList<T> values = new ArrayList<>();

        try {
            Iterator<GenericArray> replies = this.fieldStub.getArrayValue(fieldRequest);
            while (replies.hasNext()) {
                GenericArray reply = replies.next();
                List<T> chunk = getChunk(reply);
                values.addAll(chunk);
            }

        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
        return values;
    }

    protected abstract List<T> getChunk(GenericArray reply);

    @Override
    public String getJson()
    {
        return "";
    }

    @Override
    public void setJson(String jsonValue)
    {
        // Not implemented        
    }

    public void dump() {
        System.out.print("CaffaArrayField<" + dataType + ">::");
        if (this.localArray != null)
        {
            System.out.print("local");
        }
        else{
            System.out.print("grpc");
        }        
    }

    public abstract CaffaAbstractField newInstance(CaffaObject owner, String keyword);

    public Class<?> type() {
        return this.dataType;
    }
}
