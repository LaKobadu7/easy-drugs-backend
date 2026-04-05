package com.easydrugs.controller;

import com.easydrugs.entity.Notification;
import com.easydrugs.entity.Patient;
import com.easydrugs.entity.Utilisateur;
import com.easydrugs.service.NotificationService;
import com.easydrugs.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller Notification — EASY-DRUGS.
 *
 * GET  /api/notifications/me               → Mes notifications (paginées)
 * GET  /api/notifications/me/non-lues      → Notifications non lues
 * GET  /api/notifications/me/count         → Nombre de non-lues (badge)
 * PUT  /api/notifications/me/lues-toutes   → Marquer tout comme lu
 * PUT  /api/notifications/{id}/lue         → Marquer une notification comme lue
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notifications push patient")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;
    private final PatientService patientService;

    @Operation(summary = "Mes notifications (20 par page)")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Notification>> mesNotifications(
            @AuthenticationPrincipal Utilisateur utilisateur,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int taille) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        return ResponseEntity.ok(
            notificationService.findByPatient(patient.getId(), page, taille)
        );
    }

    @Operation(summary = "Notifications non lues uniquement")
    @GetMapping("/me/non-lues")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Notification>> nonLues(
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        return ResponseEntity.ok(
            notificationService.findNonLues(patient.getId())
        );
    }

    @Operation(summary = "Nombre de notifications non lues (pour le badge)")
    @GetMapping("/me/count")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Long>> countNonLues(
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        long count = notificationService.countNonLues(patient.getId());
        return ResponseEntity.ok(Map.of("nonLues", count));
    }

    @Operation(summary = "Marquer toutes les notifications comme lues")
    @PutMapping("/me/lues-toutes")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> marquerToutesLues(
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        int count = notificationService.marquerToutesLues(patient.getId());
        return ResponseEntity.ok(Map.of("marquees", count));
    }

    @Operation(summary = "Marquer une notification comme lue")
    @PutMapping("/{id}/lue")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, String>> marquerLue(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur utilisateur) {
        Patient patient = patientService.findByTelephone(utilisateur.getTelephone());
        notificationService.marquerLue(id, patient.getId());
        return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
    }
}
