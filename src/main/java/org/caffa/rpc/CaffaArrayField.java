package org.caffa.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.lang.Math;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public abstract class CaffaArrayField<T> extends CaffaField<ArrayList<T>> {
    protected GenericArray localArray = null;

    private FieldAccessGrpc.FieldAccessBlockingStub fieldStub = null;
    private FieldAccessGrpc.FieldAccessStub asyncFieldStub = null;
    private int chunkSize = 4096;

    protected CaffaArrayField(CaffaObject owner, String keyword, Type scalarType) {
        super(owner, keyword, ArrayList.class, scalarType);
    }

    @Override
    public void createAccessor(boolean grpc) {
        if (grpc) {
            if (this.owner != null) {
                this.fieldStub = FieldAccessGrpc.newBlockingStub(this.owner.channel);
                this.asyncFieldStub = FieldAccessGrpc.newStub(this.owner.channel);
            }
        } else {
            localArray = GenericArray.getDefaultInstance();
        }
    }

    @Override
    public ArrayList<T> get() {
        logger.log(Level.FINER, "Sending get request");

        if (this.localArray != null) {
            return new ArrayList<>(getChunk(this.localArray));
        }

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.owner.channel));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this.owner);
        RpcObject self = RpcObject.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(self).build();

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

    @Override
    public void set(ArrayList<T> values) {
        logger.log(Level.FINE, "Sending set request with {0} values", values.size());

        if (this.localArray != null) {
            this.localArray = createChunk(values);
            return;
        }

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.owner.channel));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this.owner);
        RpcObject self = RpcObject.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(self).build();

        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<SetterArrayReply> responseObserver = new StreamObserver<SetterArrayReply>() {
            @Override
            public void onNext(SetterArrayReply summary) {
                logger.log(Level.FINER, "Sent chunk");
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "RPC failed: {0}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.log(Level.FINE, "Completed RPC");
                finishLatch.countDown();
            }
        };

        StreamObserver<GenericArray> requestObserver = this.asyncFieldStub.setArrayValue(responseObserver);
        try {
            ArrayRequest arrayRequest = ArrayRequest.newBuilder().setField(fieldRequest).setValueCount(values.size())
                    .build();
            GenericArray initialChunk = GenericArray.newBuilder().setRequest(arrayRequest).build();

            requestObserver.onNext(initialChunk);
            for (int i = 0; i < values.size(); i += chunkSize) {
                int actualChunkSize = Math.min(chunkSize, values.size() - i);
                if (actualChunkSize > 0) {
                    GenericArray chunk = createChunk(values.subList(i, i + actualChunkSize));
                    requestObserver.onNext(chunk);
                }
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();

        try {
            // Receiving happens asynchronously
            if (!finishLatch.await(1, TimeUnit.MINUTES)) {
                logger.log(Level.SEVERE, "Set request can not finish within 1 minutes");
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted: {0}", e.getMessage());
        }
    }

    protected abstract List<T> getChunk(GenericArray reply);

    protected abstract GenericArray createChunk(List<T> values);

    @Override
    public String getJson() {
        return "";
    }

    @Override
    public void setJson(String jsonValue) {
        // Not implemented
    }

    @Override
    public void dump() {
        System.out.print("CaffaArrayField<" + this.scalarType + ">::");
        if (this.localArray != null) {
            System.out.print("local");
        } else {
            System.out.print("grpc");
        }
        System.out.println(" {");
        System.out.println("keyword = " + this.keyword);
        if (this.localArray != null) {
            System.out.println("value = " + this.localArray);
        }
        System.out.println("}");
    }
}
