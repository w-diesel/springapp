package org.foo.data.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document
public class Job {

    @Id
    private String key;
    private List payload;
    private Status status;
    private LocalDateTime сreationDate;
    private Integer criteria;

    public Job() {
    }

    public Job(String key, Integer criteria) {
        this.key = key;
        this.status = Status.PENDING;
        this.сreationDate = LocalDateTime.now();
        this.criteria = criteria;
    }


    public Integer getCriteria() {
        return criteria;
    }

    public LocalDateTime getСreationDate() {
        return сreationDate;
    }

    public void setСreationDate(LocalDateTime сreationDate) {
        this.сreationDate = сreationDate;
    }

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

    public void setCriteria(Integer criteria) {
        this.criteria = criteria;
    }

    public enum Status {
        PENDING,
        DONE
    }

}
