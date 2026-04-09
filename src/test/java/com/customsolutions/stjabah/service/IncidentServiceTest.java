package com.customsolutions.stjabah.service;

import com.customsolutions.stjabah.communication.CommunicationGateway;
import com.customsolutions.stjabah.entity.Incident;
import com.customsolutions.stjabah.entity.IncidentStatus;
import com.customsolutions.stjabah.exception.IllegalLocationUpdateException;
import com.customsolutions.stjabah.exception.IncidentNotFoundException;
import com.customsolutions.stjabah.exception.InvalidIncidentStatusTransitionException;
import com.customsolutions.stjabah.exception.InvalidStatusForDispatchException;
import com.customsolutions.stjabah.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

    @Mock
    private CommunicationGateway communicationGateway;

    @Spy
    @InjectMocks
    private IncidentService incidentService;

    @Captor
    private ArgumentCaptor<Incident> incidentCaptor;

    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void createIncident_validInput_returnIncidentWithCorrectValues(){
        incidentService.createIncident("Fire at sector 4", 30.0, 31.1);

        verify(incidentRepository).save(incidentCaptor.capture());

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
        verify(incidentRepository).findById(incidentId);
    }

    @Test
    void getIncidentById_nonExistingId_ThrowIncidentNotFound(){
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

        verify(incidentRepository).findAll();
    }

    @Test
    void getActiveIncident_activeIncidentExist_shouldReturnActiveIncident(){
        // Arrange
        List<IncidentStatus> statusList = List.of(IncidentStatus.CANCELLED, IncidentStatus.CLOSED);
        when(incidentRepository.findFirstByStatusNotIn(statusList)).thenReturn(Optional.of(new Incident()));

        // Act
        Optional<Incident> serviceReturn = incidentService.getActiveIncident();

        // Assert
        verify(incidentRepository).findFirstByStatusNotIn(statusList);
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
        verify(incidentRepository).findFirstByStatusNotIn(statusList);
        assertTrue(serviceReturn.isEmpty());
    }

    @Test
    void updateIncidentLocation_nonExistingIncident_shouldThrowIncidentNotFound(){
        // Arrange
        when(incidentRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IncidentNotFoundException.class, ()->{
            incidentService.updateIncidentLocation(100L, 123.4, 567.8);
        });

        verify(incidentRepository).findById(any());
    }

    @Test
    void updateIncidentLocation_notCreatedStatus_shouldThrowIllegalLocationUpdate(){
        // Arrange
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.DISPATCHED);
        when(incidentRepository.findById(any())).thenReturn(Optional.of(incident));

        // Act & Assert
        assertThrows(IllegalLocationUpdateException.class, ()->{
            incidentService.updateIncidentLocation(100L, 123.4, 567.8);
        });

        verify(incidentRepository).findById(any());
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

        // Assert
        verify(incidentRepository).save(incidentCaptor.capture());

        Incident modifiedIncident = incidentCaptor.getValue();

        assertEquals(567.8, modifiedIncident.getLocation().getX());
        assertEquals(123.4, modifiedIncident.getLocation().getY());
    }

    @ParameterizedTest
    @EnumSource(value=IncidentStatus.class, names={"CANCELLED", "CLOSED"})
    void cancelIncident_invalidStatus_shouldThrowInvalidIncidentStatusTransition(IncidentStatus status){
        // Arrange
        Long id = 1L;
        Incident incident = new Incident();
        incident.setStatus(status);
        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));

        // Act & Assert
        assertThrows(InvalidIncidentStatusTransitionException.class, ()->{
            incidentService.cancelIncident(id);
        });

        verify(communicationGateway, never()).broadcast(any(), any());
        verify(incidentRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value=IncidentStatus.class, names={"DISPATCHED", "ACKNOWLEDGED", "RESPONDING", "RESOLVED"})
    void cancelIncident_afterDispatch_shouldUpdateStatusAndBroadcast(IncidentStatus status){
        // Arrange
        Long id = 1L;
        Incident stubIncident = new Incident();
        stubIncident.setStatus(status);
        when(incidentRepository.findById(id)).thenReturn(Optional.of(stubIncident));

        // Act
        incidentService.cancelIncident(id);

        // Assert
        verify(communicationGateway).broadcast(any(), any());
        verify(incidentRepository).save(incidentCaptor.capture());

        Incident serviceIncident = incidentCaptor.getValue();

        assertEquals(IncidentStatus.CANCELLED, serviceIncident.getStatus());
        assertNotNull(serviceIncident.getClosedAt());
    }

    @Test
    void cancelIncident_createdState_shouldNotBroadcast(){
        // Arrange
        Long id = 1L;
        Incident stubIncident = new Incident();
        stubIncident.setStatus(IncidentStatus.CREATED);
        when(incidentRepository.findById(id)).thenReturn(Optional.of(stubIncident));

        // Act
        incidentService.cancelIncident(id);

        // Assert
        verify(communicationGateway, never()).broadcast(any(), any());
        verify(incidentRepository).save(any());
    }

    @ParameterizedTest
    @EnumSource(value=IncidentStatus.class,
            names={"DISPATCHED", "ACKNOWLEDGED", "RESPONDING", "RESOLVED", "CLOSED", "CANCELLED"})
    void dispatchIncident_invalidStatus_shouldThrowInvalidStatusForDispatch(IncidentStatus status){
        // Arrange
        Long id = 1L;
        Incident stubIncident = new Incident();
        stubIncident.setStatus(status);
        when(incidentRepository.findById(id)).thenReturn(Optional.of(stubIncident));

        // Act & Assert
        assertThrows(InvalidStatusForDispatchException.class, ()->{
            incidentService.dispatchIncident(id);
        });

        verify(communicationGateway, never()).broadcast(any(), any());
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void dispatchIncident_createdState_shouldUpdateStatusAndBroadcast(){
        // Arrange
        Long id = 1L;
        Incident stubIncident = new Incident();
        stubIncident.setStatus(IncidentStatus.CREATED);
        when(incidentRepository.findById(id)).thenReturn(Optional.of(stubIncident));

        // Act
        incidentService.dispatchIncident(id);

        // Assert
        verify(communicationGateway).broadcast(any(), any());
        verify(incidentRepository).save(incidentCaptor.capture());

        Incident serviceIncident = incidentCaptor.getValue();

        assertEquals(IncidentStatus.DISPATCHED, serviceIncident.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value=IncidentStatus.class,
            names={"CREATED", "DISPATCHED", "ACKNOWLEDGED", "RESPONDING", "CLOSED", "CANCELLED"})
    void closeIncident_invalidStatus_shouldThrowInvalidIncidentStatusTransition(IncidentStatus status){
        // Arrange
        Long id = 1L;
        Incident incident = new Incident();
        incident.setStatus(status);
        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));

        // Act & Assert
        assertThrows(InvalidIncidentStatusTransitionException.class, ()->{
            incidentService.closeIncident(id);
        });

        verify(communicationGateway, never()).broadcast(any(), any());
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void closeIncident_statusResolved_shouldUpdateStatusAndBroadcast(){
        // Arrange
        Long id = 1L;
        Incident stubIncident = new Incident();
        stubIncident.setStatus(IncidentStatus.RESOLVED);
        when(incidentRepository.findById(id)).thenReturn(Optional.of(stubIncident));

        // Act
        incidentService.closeIncident(id);

        // Assert
        verify(communicationGateway).broadcast(any(), any());
        verify(incidentRepository).save(incidentCaptor.capture());

        Incident serviceIncident = incidentCaptor.getValue();

        assertEquals(IncidentStatus.CLOSED, serviceIncident.getStatus());
        assertNotNull(serviceIncident.getClosedAt());
    }
}
