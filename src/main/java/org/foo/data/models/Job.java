package org.foo.data.models;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Job {

    private String key;
    private List payload;
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List getPayload() {
        return payload;
    }

    public void setPayload(List payload) {
        this.payload = payload;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public enum Status {
        PENDING,
        DONE
    }

}
