package org.caffa.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.grpc.StatusRuntimeException;

public abstract class CaffaArrayField<T> extends CaffaField<ArrayList<T>> {
    protected GenericArray localArray = null;

    private FieldAccessGrpc.FieldAccessBlockingStub fieldStub = null;

    protected CaffaArrayField(CaffaObject owner, String keyword, Type scalarType) {
        super(owner, keyword, ArrayList.class, scalarType);
    }

    @Override
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

    @Override
    public ArrayList<T> get() {
        logger.log(Level.FINER, "Sending get request");

        if (this.localArray != null)
        {
            return new ArrayList<>(getChunk(this.localArray));
        }
        
        FieldRequest fieldRequest = FieldRequest.newBuilder().setKeyword(keyword).setClassKeyword(this.owner.classKeyword).setUuid(this.owner.uuid).build();

        ArrayList<T> values = new ArrayList<>();

        try {
            Iterator<GenericArray> replies = this.fieldStub.getArrayValue(fieldRequest);
            while (replies.hasNext()) {
                GenericArray reply = replies.next();
                values.addAll(getChunk(reply));
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

    @Override
    public void dump() {
        System.out.print("CaffaArrayField<" + this.scalarType + ">::");
        if (this.localArray != null)
        {
            System.out.print("local");
        }
        else{
            System.out.print("grpc");
        }      
        System.out.println(" {");
        System.out.println("keyword = " + this.keyword);
        if (this.localArray != null)
        {
            System.out.println("value = " + this.localArray);
        }
        System.out.println("}");  
    }
}
