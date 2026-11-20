package com.elemental.tech.workitems.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import org.hibernate.type.descriptor.sql.internal.Scale6IntervalSecondDdlType;

/*
    TODO: Refactor this, use a library that will simplify this if we can?.
 */
@Entity
@Getter
@Table(name = "work_item")
public class WorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Priority priority;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected WorkItem() {
    }

    public WorkItem(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = Status.OPEN;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public void transitionTo(Status newStatus) {

        switch (this.status) {
            case OPEN -> {
                if (newStatus != Status.IN_PROGRESS && newStatus != Status.CANCELLED) {
                    throw new IllegalStateException(this.status + " transition to " + newStatus + " is invalid");
                }
            }
            case IN_PROGRESS -> {
                if (newStatus != Status.DONE && newStatus != Status.CANCELLED) {
                    throw new IllegalStateException(this.status + " transition to " + newStatus + " is invalid");
                }
            }
            case CANCELLED, DONE -> {
                throw new IllegalStateException("Cannot change status of CANCELLED or DONE Work Item");
            }

        }
        // TODO: enforce transitions (OPEN -> IN_PROGRESS -> DONE, etc.) and decide what to do on invalid transitions...
        this.status = newStatus;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public enum Status {
        OPEN,
        IN_PROGRESS,
        DONE,
        CANCELLED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}
