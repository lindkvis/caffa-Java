package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

public class CaffaAppEnumField extends CaffaField<CaffaAppEnum> {
    private static Logger logger = LoggerFactory.getLogger(CaffaAppEnumField.class);

    protected CaffaAppEnumField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaAppEnum.class);
    }

    public String dump(String prefix) {
        String result = prefix + "{\n";

        result += prefix + "  keyword = " + this.keyword + "\n";
        result += prefix + "  type = CaffaField<AppEnum>::";

        if (this.localValue != null) {
            result += "local\n";
        } else {
            result += "grpc\n";
        }

        if (this.localValue != null) {
            result += prefix + "  value = " + getLocalJson() + "\n";
        }
        result += prefix + "}\n";
        return result;
    }

    @Override
    public CaffaField<CaffaAppEnum> newInstance(CaffaObject owner, String keyword) {
        return new CaffaAppEnumField(owner, keyword);
    }

    public void set(String value) throws Exception {
        logger.debug("Setting string value for app enum field " + this.keyword);
        set(new CaffaAppEnum(value));
    }

    @Override
    public void set(CaffaAppEnum appEnum) throws Exception {
        logger.debug("Setting JSON for field " + this.keyword);
        super.set(appEnum);
    }

    @Override
    public String typeString() {
        return "AppEnum";
    }
}
