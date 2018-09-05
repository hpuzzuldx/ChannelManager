package com.ldx.channel;

import java.io.Serializable;

/**
 * Created by lidongxiu on 2018/6/19.
 */

public class KeywordsBean implements Serializable {
    public static final long serialVersionUID = 1L;

    public KeywordsBean() {

    }

    public KeywordsBean(int keywordId, String name) {
        this.keywordId = keywordId;
        this.name = name;
    }

    private int keywordId;
    private String name;

    public int getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(int keywordId) {
        this.keywordId = keywordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
