package com.financedomain.personnalisation.controller;

import com.financedomain.personnalisation.dto.ApiResponse;
import com.financedomain.personnalisation.service.PersonnalisationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personnalisation")
public class PersonnalisationController {

    @Autowired
    private PersonnalisationService personnalisationService;

    @Autowired
    private Environment environment;

    private String getPort() {
        return environment.getProperty("local.server.port", "unknown");
    }

    @GetMapping("/usages")
    public ResponseEntity<?> getMyUsages(
            @RequestHeader(value = "X-User-Phone", required = false) String msisdn) {
        if (msisdn == null || msisdn.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("En-tête X-User-Phone manquant ou vide.");
        }
        try {
            Object usages = personnalisationService.getClientUsages(msisdn);
            return ResponseEntity.ok(new ApiResponse<>(usages, getPort()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Profil d'usage introuvable pour le client " + msisdn + " : " + e.getMessage());
        }
    }

    @GetMapping("/usages/{msisdn}")
    public ResponseEntity<?> getUsagesByMsisdn(
            @PathVariable String msisdn,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // Un client ne peut voir que ses propres usages
        if ("CLIENT".equals(xUserRole) && !msisdn.equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : vous ne pouvez pas consulter les données d'un autre client.");
        }

        try {
            Object usages = personnalisationService.getClientUsages(msisdn);
            return ResponseEntity.ok(new ApiResponse<>(usages, getPort()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Profil d'usage introuvable pour le client " + msisdn + " : " + e.getMessage());
        }
    }
}
