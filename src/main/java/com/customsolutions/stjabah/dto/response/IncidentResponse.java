package com.customsolutions.stjabah.dto.response;

import com.customsolutions.stjabah.entity.Incident;
import com.customsolutions.stjabah.entity.IncidentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IncidentResponse {
    private Long id;
    private String title;
    private IncidentStatus status;
    private Double lat;
    private Double lng;
    private LocalDateTime createdAt;

    public static IncidentResponse from(Incident incident){
        IncidentResponse response = new IncidentResponse();

        response.setId(incident.getId());
        response.setTitle(incident.getTitle());
        response.setStatus(incident.getStatus());
        response.setLat(incident.getLocation().getY());
        response.setLng(incident.getLocation().getX());
        response.setCreatedAt(incident.getCreatedAt());
        response.setCreatedAt(incident.getClosedAt());

        return response;
    }
}