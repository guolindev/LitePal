package com.litepaltest.model;

import org.litepal.annotation.Column;

public class WeiboMessage extends Message {

    private String follower;

    @Column(ignore = true)
    private int number;

    private Cellphone cellphone;

    public String getFollower() {
        return follower;
    }

    public void setFollower(String follower) {
        this.follower = follower;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Cellphone getCellphone() {
        return cellphone;
    }

    public void setCellphone(Cellphone cellphone) {
        this.cellphone = cellphone;
    }
}
