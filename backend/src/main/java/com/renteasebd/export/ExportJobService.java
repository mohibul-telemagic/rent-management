package com.renteasebd.export;

import com.renteasebd.common.AppException;
import com.renteasebd.common.AuditService;
import com.renteasebd.domain.export.ExportJob;
import com.renteasebd.domain.export.ExportJobStatus;
import com.renteasebd.domain.user.UserRole;
import com.renteasebd.export.dto.CreateExportJobRequest;
import com.renteasebd.export.dto.ExportJobResponse;
import com.renteasebd.repository.ExportJobRepository;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExportJobService {

    private final ExportJobRepository exportJobRepository;
    private final ExportCsvService exportCsvService;
    private final AuditService auditService;
    private final Map<String, Path> exportedFiles = new ConcurrentHashMap<>();

    public ExportJobService(
        ExportJobRepository exportJobRepository,
        ExportCsvService exportCsvService,
        AuditService auditService
    ) {
        this.exportJobRepository = exportJobRepository;
        this.exportCsvService = exportCsvService;
        this.auditService = auditService;
    }

    @Transactional
    public ExportJobResponse createZipJob(CreateExportJobRequest request, Long actorUserId, UserRole role) {
        List<String> datasets = resolveDatasets(request == null ? null : request.datasets());

        ExportJob job = new ExportJob();
        job.setOwnerId(actorUserId);
        job.setStatus(ExportJobStatus.PENDING);
        job.setErrorMessage(null);
        ExportJob saved = exportJobRepository.save(job);

        auditService.log(actorUserId, "EXPORT_JOB_CREATED", "EXPORT_JOB", saved.getId(), null, Map.of("datasets", datasets));
        CompletableFuture.runAsync(() -> runZipJob(saved.getId(), actorUserId, role, datasets));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExportJobResponse getJob(String jobId, Long actorUserId) {
        ExportJob job = exportJobRepository.findByIdAndOwnerId(jobId, actorUserId)
            .orElseThrow(() -> new AppException("EXPORT_JOB_NOT_FOUND", "Export job not found", "jobId"));
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public byte[] downloadZip(String jobId, Long actorUserId) {
        ExportJob job = exportJobRepository.findByIdAndOwnerId(jobId, actorUserId)
            .orElseThrow(() -> new AppException("EXPORT_JOB_NOT_FOUND", "Export job not found", "jobId"));

        if (job.getStatus() != ExportJobStatus.COMPLETED) {
            throw new AppException("EXPORT_JOB_NOT_READY", "Export job is not completed yet", "jobId");
        }

        Path file = exportedFiles.get(jobId);
        if (file == null || !Files.exists(file)) {
            throw new AppException("EXPORT_JOB_NOT_READY", "Export artifact is not available", "jobId");
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new AppException("EXPORT_DOWNLOAD_FAILED", "Failed to read export artifact", "jobId");
        }
    }

    private void runZipJob(String jobId, Long actorUserId, UserRole role, List<String> datasets) {
        try {
            updateStatus(jobId, ExportJobStatus.RUNNING, null);

            Path zipPath = Files.createTempFile("rentease-export-" + jobId + "-", ".zip");
            try (OutputStream outputStream = Files.newOutputStream(zipPath);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                for (String dataset : datasets) {
                    byte[] csv = exportCsvService.exportDataset(dataset, actorUserId, role);
                    ZipEntry entry = new ZipEntry(dataset + ".csv");
                    zipOutputStream.putNextEntry(entry);
                    zipOutputStream.write(csv);
                    zipOutputStream.closeEntry();
                }
            }

            exportedFiles.put(jobId, zipPath);
            updateStatus(jobId, ExportJobStatus.COMPLETED, null);
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "Export failed" : ex.getMessage();
            updateStatus(jobId, ExportJobStatus.FAILED, message.length() > 500 ? message.substring(0, 500) : message);
        }
    }

    @Transactional
    protected void updateStatus(String jobId, ExportJobStatus status, String errorMessage) {
        ExportJob job = exportJobRepository.findById(jobId)
            .orElseThrow(() -> new AppException("EXPORT_JOB_NOT_FOUND", "Export job not found", "jobId"));
        job.setStatus(status);
        job.setErrorMessage(errorMessage);
        exportJobRepository.save(job);
    }

    private List<String> resolveDatasets(List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return exportCsvService.supportedDatasets();
        }

        Set<String> dedup = new LinkedHashSet<>();
        for (String item : requested) {
            if (item == null || item.isBlank()) {
                continue;
            }
            String normalized = item.trim().toLowerCase();
            if (!exportCsvService.supportedDatasets().contains(normalized)) {
                throw new AppException("INVALID_DATASET", "Unsupported dataset in export job", "datasets");
            }
            dedup.add(normalized);
        }

        if (dedup.isEmpty()) {
            throw new AppException("INVALID_DATASET", "At least one dataset is required", "datasets");
        }
        return List.copyOf(dedup);
    }

    private ExportJobResponse toResponse(ExportJob job) {
        return new ExportJobResponse(
            job.getId(),
            job.getStatus(),
            job.getErrorMessage(),
            job.getStatus() == ExportJobStatus.COMPLETED
        );
    }
}
