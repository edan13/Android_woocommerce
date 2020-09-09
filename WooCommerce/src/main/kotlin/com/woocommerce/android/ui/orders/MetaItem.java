package com.woocommerce.android.ui.orders;

import java.util.List;

public class MetaItem {

    private List<ItemBean> metaList;

    public List<ItemBean> getMetaList() {
        return metaList;
    }

    public void setMetaList(List<ItemBean> metaList) {
        this.metaList = metaList;
    }

    public static class ItemBean{
        private long id;
        private String key;
        private String value;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
