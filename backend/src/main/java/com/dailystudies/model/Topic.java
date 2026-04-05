package com.dailystudies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "daily_studies")
public class Topic {

    @Id
    @Column(name = "topic")
    private String topic;

    @Column(name = "count", nullable = false)
    private int count;

    @Column(name = "item_to_study_next")
    private String itemToStudyNext;

    @Column(name = "last_updated_date", nullable = false)
    private LocalDate lastUpdatedDate;

    @Column(name = "last_studied_date")
    private LocalDate lastStudiedDate;

    public Topic(String topic) {
        this.topic = topic;
        this.count = 0;
        this.lastUpdatedDate = LocalDate.now();
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getItemToStudyNext() {
        return itemToStudyNext;
    }

    public void setItemToStudyNext(String item) {
        this.itemToStudyNext = item;
    }

    public LocalDate getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(LocalDate d) {
        this.lastUpdatedDate = d;
    }

    public LocalDate getLastStudiedDate() {
        return lastStudiedDate;
    }

    public void setLastStudiedDate(LocalDate lastStudiedDate) {
        this.lastStudiedDate = lastStudiedDate;
    }
}
