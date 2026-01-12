package com.moveit.championship.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Table(name = "championship")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Championship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer id;

    @OneToMany(mappedBy = "championship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Competition> competitions;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Date startDate;

    @Column(nullable = false)
    private Date endDate;

    @Enumerated(EnumType.STRING)
    private Status status;
}