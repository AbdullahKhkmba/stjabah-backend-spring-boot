package com.customsolutions.stjabah.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateIncidentRequest {
    @NotBlank
    private String title;

    @NotNull
    @DecimalMin(value="-90.0")
    @DecimalMax(value="90.0")
    private Double lat;

    @NotNull
    @DecimalMin(value="-180.0")
    @DecimalMax(value="180.0")
    private Double lng;
}
