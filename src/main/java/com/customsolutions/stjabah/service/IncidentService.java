package com.customsolutions.stjabah.service;

import com.customsolutions.stjabah.communication.CommunicationGateway;
import com.customsolutions.stjabah.entity.Incident;
import com.customsolutions.stjabah.entity.IncidentStatus;
import com.customsolutions.stjabah.exception.IllegalLocationUpdateException;
import com.customsolutions.stjabah.exception.IncidentNotFoundException;
import com.customsolutions.stjabah.exception.InvalidIncidentStatusTransitionException;
import com.customsolutions.stjabah.repository.IncidentRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final CommunicationGateway communicationGateway;
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public IncidentService(IncidentRepository incidentRepository, CommunicationGateway communicationGateway){
        this.incidentRepository = incidentRepository;
        this.communicationGateway = communicationGateway;
    }

    public Incident createIncident(String title, double lat, double lng){
        Incident theIncident = new Incident();
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));  // notice the swap (lng, lat) not (lat, lng)

        theIncident.setTitle(title);
        theIncident.setStatus(IncidentStatus.CREATED);
        theIncident.setLocation(point);

        return incidentRepository.save(theIncident);
    }

    public Incident getIncidentById(Long id){
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(id));
    }

    public List<Incident> getAllIncidents(){
        return incidentRepository.findAll();
    }

    public Optional<Incident> getActiveIncident(){
        List<IncidentStatus> statusList = List.of(IncidentStatus.CANCELLED, IncidentStatus.CLOSED);
        return incidentRepository.findFirstByStatusNotIn(statusList);
    }

    public Incident updateIncidentLocation(Long id, double lat, double lng){
        Incident tempIncident = getIncidentById(id);

        if(tempIncident.getStatus() != IncidentStatus.CREATED)
            throw new IllegalLocationUpdateException(id);

        Point newLocation = geometryFactory.createPoint(new Coordinate(lng, lat));
        tempIncident.setLocation(newLocation);

        return incidentRepository.save(tempIncident);
    }

    public Incident cancelIncident(Long id) {
        Incident tempIncident = getIncidentById(id);
        List<IncidentStatus> invalidStatuses = List.of(IncidentStatus.CANCELLED, IncidentStatus.CLOSED);

        if (invalidStatuses.contains(tempIncident.getStatus()))
            throw new InvalidIncidentStatusTransitionException(tempIncident.getStatus(), IncidentStatus.CANCELLED);

        IncidentStatus previousStatus = tempIncident.getStatus();

        tempIncident.setStatus(IncidentStatus.CANCELLED);
        tempIncident.setClosedAt(LocalDateTime.now());

        if (previousStatus != IncidentStatus.CREATED)
            communicationGateway.broadcast("ert/incident/cancelled", tempIncident);

        return incidentRepository.save(tempIncident);
    }

}
