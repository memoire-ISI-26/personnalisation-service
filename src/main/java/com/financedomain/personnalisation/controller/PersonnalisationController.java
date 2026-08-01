package com.financedomain.personnalisation.controller;

import com.financedomain.personnalisation.dto.ApiResponse;
import com.financedomain.personnalisation.service.PersonnalisationService;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personnalisation")
public class PersonnalisationController {

    private final PersonnalisationService personnalisationService;

    private final Environment environment;

    public PersonnalisationController(PersonnalisationService personnalisationService, Environment environment) {
        this.personnalisationService = personnalisationService;
        this.environment = environment;
    }

    private String getPort() {
        return environment.getProperty("local.server.port", "unknown");
    }

    @GetMapping("/usages")
    public ResponseEntity<Object> getMyUsages(
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

    @GetMapping("/usages/stats/global")
    public ResponseEntity<Object> getGlobalStats(
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            Object stats = personnalisationService.getGlobalStats();
            return ResponseEntity.ok(new ApiResponse<>(stats, getPort()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des stats globales HDFS : " + e.getMessage());
        }
    }

    @GetMapping("/usages/{msisdn}")
    public ResponseEntity<Object> getUsagesByMsisdn(
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

    @GetMapping("/default-services")
    public ResponseEntity<Object> getDefaultServices() {
        try {
            return ResponseEntity.ok(new ApiResponse<>(personnalisationService.getDefaultServices(), getPort()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des services par défaut : " + e.getMessage());
        }
    }

    @PostMapping("/default-services")
    public ResponseEntity<Object> saveDefaultServices(
            @RequestBody java.util.List<com.financedomain.personnalisation.dto.DefaultServicesDto> dtos,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        
        if (xUserRole == null || !"ADMINISTRATOR".equals(xUserRole)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Accès refusé : rôle administrateur requis.");
        }

        try {
            personnalisationService.saveDefaultServices(dtos);
            return ResponseEntity.ok(new ApiResponse<>("Configuration enregistrée avec succès.", getPort()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'enregistrement de la configuration : " + e.getMessage());
        }
    }
}
