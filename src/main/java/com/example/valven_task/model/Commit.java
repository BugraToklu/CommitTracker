package com.example.valven_task.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "commits")
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sha;
    private String authorName;
    private String authorEmail;

    //@Lob
    @Column(columnDefinition = "text")
    private String message;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timestamp;

    //private LocalDateTime timestamp;
    private String repositoryName;
    private String provider;

    //@Lob
    @Column(columnDefinition = "text")
    private String patch;

    @ManyToOne
    @JoinColumn(name = "developer_id")
    private Developer developer;
}
