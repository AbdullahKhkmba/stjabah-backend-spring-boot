package com.customsolutions.stjabah.controller;

import com.customsolutions.stjabah.dto.request.CreateIncidentRequest;
import com.customsolutions.stjabah.dto.response.IncidentResponse;
import com.customsolutions.stjabah.entity.Incident;
import com.customsolutions.stjabah.service.IncidentService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService){
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@RequestBody @Valid CreateIncidentRequest request){
        Incident incident = incidentService.createIncident(request.getTitle(), request.getLat(), request.getLng());
        IncidentResponse response = IncidentResponse.from(incident);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getAllIncidents(){
        List<Incident> incidentList = incidentService.getAllIncidents();
        List<IncidentResponse> responseList = incidentList.stream()
                .map(IncidentResponse::from)
                .toList();

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }
}
