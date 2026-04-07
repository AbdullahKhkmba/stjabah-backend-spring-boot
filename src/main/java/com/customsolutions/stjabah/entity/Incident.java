package com.customsolutions.stjabah.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name="incident")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title", nullable = false)
    private String title;

    @Column(name="location", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private IncidentStatus status;

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    private void onCreate(){
        this.createdAt = LocalDateTime.now();
    }


}
