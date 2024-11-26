package com.Ron.tradingApps.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "instructor")
public class Instructor extends AppsUser {

    @Column(name = "instructor_number")
    private String instructorNumber;


    @Builder
    public Instructor(String username, String email, String password, String firstName, String lastName, LocalDateTime createdAt, LocalDateTime updatedAt, String userId, String instructorNumber) {
        super(username, email, password, firstName, lastName, createdAt, updatedAt, userId);
        this.instructorNumber = instructorNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instructor that)) return false;
        return Objects.equals(getInstructorNumber(), that.getInstructorNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstructorNumber());
    }
}
