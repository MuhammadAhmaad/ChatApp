package com.companyname.chatapp.chatapp;

/**
 * Created by Mohamed Ahmed on 6/30/2018.
 */

public class Requests {
    private String request_type;
    public Requests()
    {

    }

    public Requests(String request_type) {
        this.request_type = request_type;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
