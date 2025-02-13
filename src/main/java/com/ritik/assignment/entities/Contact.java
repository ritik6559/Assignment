package com.ritik.assignment.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;

    private String email;

    @Column(name = "linked_id")
    private Long linkedId;

    @Enumerated(EnumType.STRING)
    private LinkPrecedence linkPrecedence;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime deletedAt = LocalDateTime.now();

    public enum LinkPrecedence {
        PRIMARY, SECONDARY
    }

}

