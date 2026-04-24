package com.renteasebd.export.dto;

import com.renteasebd.domain.export.ExportJobStatus;

public record ExportJobResponse(
    String id,
    ExportJobStatus status,
    String errorMessage,
    boolean downloadable
) {
}
