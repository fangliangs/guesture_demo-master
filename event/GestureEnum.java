package com.example.android.tflitecamerademo.event;

public enum GestureEnum {

    TYPE_FIVE("five","five"),
    TYPE_OK("ok","ok"),
    TYPE_STOP("stop","stop"),
    TYPE_XIN("xin","xin"),
    TYPE_YE("ye","ye");

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    private boolean selected;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String type;
    private String value;
    GestureEnum(String type, String value) {
        this.type = type;
        this.value = value;

    }


    /**
     * 根据key获取value
     *
     * @param key
     *            : 键值key
     * @return String
     */
    public static String getValueByKey(String key) {
        GestureEnum[] enums = GestureEnum.values();
        for (int i = 0; i < enums.length; i++) {
            if (enums[i].getType().equals(key)) {
                return enums[i].getValue();
            }
        }
        return "";
    }

}
