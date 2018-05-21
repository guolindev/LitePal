package com.litepaltest.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class Message extends LitePalSupport {

    private int id;

    private String content;

    public int type;

    @Column(ignore = true)
    String title;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
