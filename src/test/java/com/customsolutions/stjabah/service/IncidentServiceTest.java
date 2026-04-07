package com.customsolutions.stjabah.service;

import com.customsolutions.stjabah.entity.Incident;
import com.customsolutions.stjabah.entity.IncidentStatus;
import com.customsolutions.stjabah.exception.IllegalLocationUpdateException;
import com.customsolutions.stjabah.exception.IncidentNotFoundException;
import com.customsolutions.stjabah.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {
    @Mock
    private IncidentRepository incidentRepository;

    @Spy
    @InjectMocks
    private IncidentService incidentService;

    @Captor
    private ArgumentCaptor<Incident> incidentCaptor;

    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void createIncident_validInput_returnIncidentWithCorrectValues(){
        incidentService.createIncident("Fire at sector 4", 30.0, 31.1);

        verify(incidentRepository, times(1)).save(incidentCaptor.capture());

        Incident savedIncident = incidentCaptor.getValue();

        assertEquals("Fire at sector 4", savedIncident.getTitle());
        assertEquals(31.1, savedIncident.getLocation().getX());
        assertEquals(30.0, savedIncident.getLocation().getY());
        assertEquals(IncidentStatus.CREATED, savedIncident.getStatus());
        assertNull(savedIncident.getClosedAt());
    }

    @Test
    void getIncidentById_existingId_returnIncident(){
        // Arrange
        Long incidentId = 10L;
        Incident repoIncident = new Incident();
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(repoIncident));

        // Act
        Incident serviceIncident = incidentService.getIncidentById(incidentId);

        // Assert
        assertSame(repoIncident, serviceIncident);
        verify(incidentRepository, times(1)).findById(incidentId);
    }

    @Test
    void getIncidentById_nonExistingId_ThrowIncidentNotFoundException(){
        // Arrange
        Long incidentId = 100L;
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IncidentNotFoundException.class, ()->{
            incidentService.getIncidentById(incidentId);
        });
    }

    @Test
    void getAllIncidents_shouldReturnList(){
        // Arrange
        Incident incident1 = new Incident();
        Incident incident2 = new Incident();
        Incident incident3 = new Incident();

        List<Incident> incidentList = List.of(incident1, incident2, incident3);
        when(incidentRepository.findAll()).thenReturn(incidentList);

        // Act
        List<Incident> returnedList = incidentService.getAllIncidents();

        // Assert
        assertEquals(3, returnedList.size());
        assertEquals(incidentList, returnedList);

        verify(incidentRepository, times(1)).findAll();
    }

    @Test
    void getActiveIncident_activeIncidentExist_shouldReturnActiveIncident(){
        // Arrange
        List<IncidentStatus> statusList = List.of(IncidentStatus.CANCELLED, IncidentStatus.CLOSED);
        when(incidentRepository.findFirstByStatusNotIn(statusList)).thenReturn(Optional.of(new Incident()));

        // Act
        Optional<Incident> serviceReturn = incidentService.getActiveIncident();

        // Assert
        verify(incidentRepository, times(1)).findFirstByStatusNotIn(statusList);
        assertTrue(serviceReturn.isPresent());
    }

    @Test
    void getActiveIncident_noActiveIncident_shouldReturnEmptyOptional(){
        // Arrange
        List<IncidentStatus> statusList = List.of(IncidentStatus.CANCELLED, IncidentStatus.CLOSED);
        when(incidentRepository.findFirstByStatusNotIn(statusList)).thenReturn(Optional.empty());

        // Act
        Optional<Incident> serviceReturn = incidentService.getActiveIncident();

        // Assert
        verify(incidentRepository, times(1)).findFirstByStatusNotIn(statusList);
        assertTrue(serviceReturn.isEmpty());
    }

    @Test
    void updateIncidentLocation_nonExistingIncident_shouldThrowIncidentNotFoundException(){
        // Arrange
        when(incidentRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IncidentNotFoundException.class, ()->{
            incidentService.updateIncidentLocation(100L, 123.4, 567.8);
        });

        verify(incidentRepository, times(1)).findById(any());
    }

    @Test
    void updateIncidentLocation_notCreatedStatus_shouldThrowIllegalLocationUpdateException(){
        // Arrange
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.DISPATCHED);
        when(incidentRepository.findById(any())).thenReturn(Optional.of(incident));

        // Act & Assert
        assertThrows(IllegalLocationUpdateException.class, ()->{
            incidentService.updateIncidentLocation(100L, 123.4, 567.8);
        });

        verify(incidentRepository, times(1)).findById(any());
    }

    @Test
    void updateIncidentLocation_validInput_shouldReturnUpdatedIncident(){
        // Arrange
        Point location = geometryFactory.createPoint(new Coordinate(10.1, 20.2));

        Incident incident = new Incident();
        incident.setId(10L);
        incident.setStatus(IncidentStatus.CREATED);
        incident.setLocation(location);

        when(incidentRepository.findById(10L)).thenReturn(Optional.of(incident));

        // Act
        incidentService.updateIncidentLocation(10L, 123.4, 567.8);
        verify(incidentRepository, times(1)).save(incidentCaptor.capture());
        Incident modifiedIncident = incidentCaptor.getValue();

        // Assert
        assertEquals(567.8, modifiedIncident.getLocation().getX());
        assertEquals(123.4, modifiedIncident.getLocation().getY());
    }
}
