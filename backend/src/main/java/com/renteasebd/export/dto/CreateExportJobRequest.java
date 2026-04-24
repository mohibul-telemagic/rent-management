package com.renteasebd.export.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateExportJobRequest(
    @Size(max = 10) List<@Size(min = 3, max = 20) String> datasets
) {
}
