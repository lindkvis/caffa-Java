package org.caffa.rpc;

import com.google.gson.GsonBuilder;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.logging.Level;

public class CaffaAppEnumField extends CaffaField<CaffaAppEnum> {
    private ArrayList<String> validValues = new ArrayList<String>();

    protected CaffaAppEnumField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaAppEnum.class);
    }

    public void addValidValue(String validValue) {
        validValues.add(validValue);
    }

    public void dump() {
        System.out.print("CaffaField<AppEnum>(");
        for (int index = 0; index < validValues.size(); ++index) {
            if (index > 0) {
                System.out.print(",");
            }
            System.out.print(validValues.get(index));
        }
        System.out.print(")::");

        if (this.localValue != null) {
            System.out.print("local");
        } else {
            System.out.print("grpc");
        }

        System.out.println(" {");
        System.out.println("keyword = " + this.keyword);
        if (this.localValue != null) {
            System.out.println("value = " + this.localValue);
        }
        System.out.println("}");
    }

    @Override
    public CaffaField<CaffaAppEnum> newInstance(CaffaObject owner, String keyword) {
        return new CaffaAppEnumField(owner, keyword);
    }

    public void set(String value) throws Exception {
        logger.log(Level.FINER, "Setting string value for app enum field " + this.keyword);
        set(new CaffaAppEnum(value));
    }

    @Override
    public void set(CaffaAppEnum appEnum) throws Exception {
        logger.log(Level.FINER, "Setting JSON for field " + this.keyword);
        if (!this.validValues.contains(appEnum.value())) {
            String errMsg = "The enum value " + appEnum.value() + " is not valid";
            logger.log(Level.SEVERE, errMsg);
            throw new IllegalArgumentException(errMsg);
        }

        setJson(new GsonBuilder().create().toJson(appEnum.value()));
    }

    @Override
    public String typeString()
    {
        String validValueString = "";
        for (String validValue : validValues)
        {
            if (!validValueString.isEmpty()) validValueString += ",";
            validValueString += validValue;
        }
        return "AppEnum(" + validValueString + ")";
    }
}
