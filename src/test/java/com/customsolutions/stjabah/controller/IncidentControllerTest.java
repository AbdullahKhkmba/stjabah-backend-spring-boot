package com.customsolutions.stjabah.controller;

import com.customsolutions.stjabah.entity.Incident;
import com.customsolutions.stjabah.entity.IncidentStatus;
import com.customsolutions.stjabah.service.IncidentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
public class IncidentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    private GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    static Stream<String> invalidBodies(){
        return Stream.of(
                // Missing Field Tests
                // missing title
            """
                { "lat": 10.0, "lng": 12.2 }
               """,
                // missing lat
                """
                { "title": "Fire at Sector 4", "lng": 12.2 }
               """,
                // missing lng
                """
                { "title": "Fire at Sector 4", "lat": 10.0 }
               """,

                // Null & Blank Tests
                // title null
                """
                { "title": null, "lat": 10.0, "lng": 12.2 }
               """,
                // title blank
                """
                { "title": "", "lat": 10.0, "lng": 12.2 }
               """,
                // lat null
                """
                { "title": "Fire", "lat": null, "lng": 12.2 }
               """,
                // lng null
                """
                { "title": "Fire", "lat": 10.0, "lng": null }
               """,

                // Max & Min Tests
                // lat out of minimum range
                """
                { "title": "Fire", "lat": -91.0, "lng": 12.2 }
               """,
                // lng out of minimum range
                """
                { "title": "Fire", "lat": 10.0, "lng": -181.0 }
               """,
                // lat out of maximum range
                """
                { "title": "Fire", "lat": 91.0, "lng": 12.2 }
               """,
                // lng out of maximum range
                """
                { "title": "Fire", "lat": 10.0, "lng": 181.0 }
               """
        );
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    void createIncident_invalidBodies_returnBadRequest(String invalidBody) throws Exception {
        // Act & Assert
        mockMvc.perform(
                post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody)
        )
                .andExpect(status().isBadRequest());

        verify(incidentService, never()).createIncident(any(), anyDouble(), anyDouble());
    }

    @Test
    void createIncident_validBody_returnCreated() throws Exception {
        // Arrange
        String title = "Fire at sector 4";

        Incident serviceIncident = new Incident();
        Point location = geometryFactory.createPoint(new Coordinate(11.1, 10.0));

        serviceIncident.setId(1L);
        serviceIncident.setTitle(title);
        serviceIncident.setLocation(location);
        serviceIncident.setStatus(IncidentStatus.CREATED);
        serviceIncident.setCreatedAt(LocalDateTime.now());

        given(incidentService.createIncident(title,10.0,11.1)).willReturn(serviceIncident);

        // Act
        mockMvc.perform(
                post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Fire at sector 4",
                                    "lat": 10.0,
                                    "lng": 11.1
                                }
                                """)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.lat").value(10.0))
                .andExpect(jsonPath("$.lng").value(11.1))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(incidentService).createIncident(title, 10.0, 11.1);
    }

}
