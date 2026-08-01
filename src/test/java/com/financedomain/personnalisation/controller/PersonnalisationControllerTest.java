package com.financedomain.personnalisation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financedomain.personnalisation.dto.DefaultServicesDto;
import com.financedomain.personnalisation.service.PersonnalisationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PersonnalisationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PersonnalisationService personnalisationService;

    @Mock
    private Environment environment;

    @InjectMocks
    private PersonnalisationController personnalisationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(personnalisationController).build();
    }

    @Test
    @DisplayName("GET /personnalisation/usages - Devrait retourner 200 OK si l'entête X-User-Phone est fourni")
    void shouldReturnUsagesWhenMsisdnHeaderPresent() throws Exception {
        String msisdn = "771234567";
        List<Object> usages = new ArrayList<>();
        when(personnalisationService.getClientUsages(msisdn)).thenReturn(usages);

        mockMvc.perform(get("/personnalisation/usages")
                        .header("X-User-Phone", msisdn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /personnalisation/usages - Devrait retourner 400 Bad Request si l'entête X-User-Phone est absent")
    void shouldReturnBadRequestWhenMsisdnHeaderMissing() throws Exception {
        mockMvc.perform(get("/personnalisation/usages"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("En-tête X-User-Phone manquant ou vide."));
    }

    @Test
    @DisplayName("GET /personnalisation/usages/stats/global - Devrait retourner 200 OK pour les statistiques globales si rôle présent")
    void shouldReturnGlobalStatsWhenRoleHeaderPresent() throws Exception {
        when(personnalisationService.getGlobalStats()).thenReturn(new HashMap<>());

        mockMvc.perform(get("/personnalisation/usages/stats/global")
                        .header("X-User-Role", "ADMINISTRATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("GET /personnalisation/usages/stats/global - Devrait retourner 401 Unauthorized si le rôle est absent")
    void shouldReturnUnauthorizedForGlobalStatsWhenRoleMissing() throws Exception {
        mockMvc.perform(get("/personnalisation/usages/stats/global"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    @DisplayName("GET /personnalisation/usages/{msisdn} - Devrait autoriser un client à consulter ses propres usages")
    void shouldAllowClientToViewOwnUsagesByMsisdn() throws Exception {
        String msisdn = "771234567";
        when(personnalisationService.getClientUsages(msisdn)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/personnalisation/usages/" + msisdn)
                        .header("X-User-Role", "CLIENT")
                        .header("X-User-Phone", msisdn))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /personnalisation/usages/{msisdn} - Devrait refuser l'accès 403 à un client consultant les usages d'un autre")
    void shouldForbiddenClientFromViewingOtherClientUsages() throws Exception {
        mockMvc.perform(get("/personnalisation/usages/779999999")
                        .header("X-User-Role", "CLIENT")
                        .header("X-User-Phone", "771234567"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Accès refusé : vous ne pouvez pas consulter les données d'un autre client."));
    }

    @Test
    @DisplayName("GET /personnalisation/default-services - Devrait retourner la liste des services par défaut")
    void shouldReturnDefaultServices() throws Exception {
        when(personnalisationService.getDefaultServices()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/personnalisation/default-services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /personnalisation/default-services - Devrait autoriser un administrateur à sauvegarder")
    void shouldAllowAdminToSaveDefaultServices() throws Exception {
        List<DefaultServicesDto> dtos = new ArrayList<>();

        mockMvc.perform(post("/personnalisation/default-services")
                        .header("X-User-Role", "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Configuration enregistrée avec succès."));
    }

    @Test
    @DisplayName("POST /personnalisation/default-services - Devrait refuser l'accès 401 à un non-administrateur")
    void shouldRejectNonAdminFromSavingDefaultServices() throws Exception {
        mockMvc.perform(post("/personnalisation/default-services")
                        .header("X-User-Role", "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isUnauthorized());
    }
}
