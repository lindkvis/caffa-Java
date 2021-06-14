package org.caffa.rpc;

import org.caffa.rpc.CaffaAbstractField;
import org.caffa.rpc.FieldRequest;
import org.caffa.rpc.Object;
import org.caffa.rpc.GetterReply;
import org.caffa.rpc.SetterChunk;
import org.caffa.rpc.SetterRequest;
import org.caffa.rpc.SetterReply;

import io.grpc.ManagedChannel;

import java.util.Iterator;

public class CaffaValueField extends CaffaAbstractField {

    CaffaValueField(CaffaObject owner, ManagedChannel channel) {
        super(owner, channel);
    }

    Iterator<GetterReply> get() {
        FieldRequest request = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(this.owner.object).build();
        return this.fieldStub.getValue(request);
    }
}
