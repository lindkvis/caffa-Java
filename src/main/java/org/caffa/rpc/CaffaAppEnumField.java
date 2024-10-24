package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class CaffaAppEnumField extends CaffaField<CaffaAppEnum> {
    private final ArrayList<String> validValues = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(CaffaAppEnumField.class);

    protected CaffaAppEnumField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaAppEnum.class);
    }

    public void addValidValue(String validValue) {
        validValues.add(validValue);
    }

    public String dump(String prefix) {
        StringBuilder result = new StringBuilder(prefix + "{\n");

        result.append(prefix).append("  keyword = ").append(this.keyword).append("\n");
        result.append(prefix).append("  type = CaffaField<AppEnum>(");
        for (int index = 0; index < validValues.size(); ++index) {
            if (index > 0) {
                result.append(",");
            }
            result.append(validValues.get(index));
        }
        result.append(")::");

        if (this.isLocalField()) {
            result.append("local\n");
        } else {
            result.append("rpc\n");
        }

        result.append(prefix).append("  value = ").append(getLocalJson()).append("\n");
        result.append(prefix).append("}\n");
        return result.toString();
    }

    @Override
    public CaffaField<CaffaAppEnum> newInstance(CaffaObject owner, String keyword) {
        return new CaffaAppEnumField(owner, keyword);
    }

    public void set(String value) throws Exception {
        logger.debug("Setting string value for app enum field {}", this.keyword);
        set(new CaffaAppEnum(value));
    }

    @Override
    public void set(CaffaAppEnum appEnum) throws Exception {
        logger.debug("Setting JSON for field {}", this.keyword);
        if (!this.validValues.contains(appEnum.value())) {
            String errMsg = "The enum value " + appEnum.value() + " is not valid";
            logger.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        super.set(appEnum);
    }
}
