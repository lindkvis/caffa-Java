package org.caffa.rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;
import io.grpc.stub.StreamObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public abstract class CaffaArrayField<T> extends CaffaField<ArrayList<T>> {
    protected GenericArray localArray = null;
    private static Logger logger = LoggerFactory.getLogger(CaffaArrayField.class);

    private FieldAccessGrpc.FieldAccessBlockingStub fieldBlockingStub = null;
    private FieldAccessGrpc.FieldAccessStub fieldStub = null;
    public static int chunkSize = 8192;

    protected CaffaArrayField(CaffaObject owner, String keyword, Type scalarType) {
        super(owner, keyword, ArrayList.class, scalarType);
    }

    @Override
    public void createAccessor(boolean grpc) {
        if (grpc) {
            if (this.owner != null) {
                this.fieldBlockingStub = FieldAccessGrpc.newBlockingStub(this.owner.channel);
                this.fieldStub = FieldAccessGrpc.newStub(this.owner.channel);
            }
        } else {
            localArray = GenericArray.getDefaultInstance();
            localValue = "";
        }
    }

    @Override
    public ArrayList<T> get() {
        if (this.localArray != null) {
             return new ArrayList<>(getChunk(this.localArray));
        }
        logger.debug("Sending get request");

        FieldRequest fieldRequest = FieldRequest.newBuilder().setKeyword(keyword)
                .setClassKeyword(this.owner.classKeyword).setUuid(this.owner.uuid).build();

        ArrayList<T> values = new ArrayList<>();

        try {
            Iterator<GenericArray> replies = this.fieldBlockingStub.getArrayValue(fieldRequest);
            while (replies.hasNext()) {
                GenericArray reply = replies.next();
                values.addAll(getChunk(reply));
            }

        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {0}", e.getStatus());
        }
        return values;
    }

    @Override
    public void set(ArrayList<T> values) {
        logger.debug("Sending get request");

        if (this.localArray != null) {
            this.localArray = createChunk(values);
            return;
        }

        int chunkCount = values.size() / chunkSize;
        if (values.size() % chunkSize != 0)
            chunkCount++;

        logger.debug("Attempting to send {0} values in {1} chunks",
                new Object[] { values.size(), chunkCount });

        FieldRequest fieldRequest = FieldRequest.newBuilder().setKeyword(keyword)
                .setClassKeyword(this.owner.classKeyword).setUuid(this.owner.uuid).build();
        ArrayRequest setterRequest = ArrayRequest.newBuilder().setField(fieldRequest).setValueCount(values.size())
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<SetterArrayReply> responseObserver = new StreamObserver<SetterArrayReply>() {
            @Override
            public void onNext(SetterArrayReply reply) {
                logger.debug("Sent {0} values", reply.getValueCount());
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                if (status.getCode() != Code.OUT_OF_RANGE) {
                    logger.error("Error sending chunk: {0}", status);
                } else {
                    logger.debug("Sent all values");
                }
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.debug("Sent all values");
                finishLatch.countDown();

            }
        };

        StreamObserver<GenericArray> requestObserver = this.fieldStub.setArrayValue(responseObserver);
        try {
            GenericArray initMsg = GenericArray.newBuilder().setRequest(setterRequest).build();
            requestObserver.onNext(initMsg);

            for (int i = 0; i < chunkCount; ++i) {
                int fromIndex = i * chunkSize;
                int toIndex = Math.min(fromIndex + chunkSize, values.size());
                List<T> subList = values.subList(fromIndex, toIndex);
                requestObserver.onNext(createChunk(subList));
                if (finishLatch.getCount() == 0) {
                    break;
                }
            }
            requestObserver.onCompleted();
            finishLatch.await(30, TimeUnit.SECONDS);

        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {0}", e.getStatus());
            requestObserver.onError(e);
        } catch (Exception e) {
            logger.warn("Something failed: {0}", e.getMessage());
            requestObserver.onError(e);
        }
    }

    protected abstract List<T> getChunk(GenericArray reply);
    protected abstract List<T> getListFromJsonArray(JsonArray jsonArray);

    protected abstract GenericArray createChunk(List<T> reply);
    
    public abstract JsonArray getJsonArray();

    @Override
    public String getJson() {
        return getJsonArray().toString();
    }

    @Override
    public void setJson(String jsonValue) {
        JsonElement jsonArrayElement = JsonParser.parseString(jsonValue);
        List<T> values = getListFromJsonArray(jsonArrayElement.getAsJsonArray());
        this.localArray = createChunk(values);
        this.localValue = jsonValue;
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

    @Override
    public String typeString()
    {
        String scalarString = super.typeString();
        return scalarString + "[]";
    }
}
