package com.moveit.championship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveit.championship.entity.Championship;
import com.moveit.championship.entity.Status;
import com.moveit.championship.mother.ChampionshipMother;
import com.moveit.championship.service.ChampionshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChampionshipController.class)
@Import(TestSecurityConfig.class)
public class ChampionshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChampionshipService championshipService;

    private Championship championship1;
    private Championship championship2;

    @BeforeEach
    void setUp() {
        championship1 = ChampionshipMother.championship().build();

        championship2 = ChampionshipMother.championship()
                .withId(2)
                .build();
    }

    @Test
    @DisplayName("Should retrieve all championships successfully.")
    void testGetAllChampionships_Success() throws Exception {
        when(championshipService.getAllChampionships()).thenReturn(List.of(championship1, championship2));

        mockMvc.perform(get("/championships")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", equalTo(championship1.getId())))
                .andExpect(jsonPath("$[0].name", equalTo(championship1.getName())))
                .andExpect(jsonPath("$[1].id", equalTo(championship2.getId())))
                .andExpect(jsonPath("$[1].name", equalTo(championship2.getName())));

        verify(championshipService, times(1)).getAllChampionships();
    }

    @Test
    @DisplayName("Should retrieve empty list when no championships exist.")
    void testGetAllChampionships_Empty() throws Exception {
        when(championshipService.getAllChampionships()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/championships")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(championshipService, times(1)).getAllChampionships();
    }

    @Test
    @DisplayName("Should retrieve championship by ID successfully.")
    void testGetChampionshipById_Success() throws Exception {
        when(championshipService.getChampionshipById(championship1.getId())).thenReturn(championship1);

        mockMvc.perform(get("/championships/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(championship1.getId())))
                .andExpect(jsonPath("$.name", equalTo(championship1.getName())))
                .andExpect(jsonPath("$.description", equalTo(championship1.getDescription())))
                .andExpect(jsonPath("$.status", equalTo(championship1.getStatus())));

        verify(championshipService, times(1)).getChampionshipById(1);
    }

    @Test
    @DisplayName("Should return 404 when championship by ID not found.")
    void testGetChampionshipById_NotFound() throws Exception {
        when(championshipService.getChampionshipById(999)).thenReturn(null);

        mockMvc.perform(get("/championships/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(championshipService, times(1)).getChampionshipById(999);
    }

    @Test
    @DisplayName("Should create championship successfully.")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateChampionship_Success() throws Exception {
        when(championshipService.createChampionship(any(Championship.class)))
                .thenReturn(championship1);

        mockMvc.perform(post("/championships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(championship2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(championship2.getId())))
                .andExpect(jsonPath("$.name", equalTo(championship2.getName())));

        verify(championshipService, times(1)).createChampionship(any(Championship.class));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when creating championship without authentication.")
    void testCreateChampionship_Unauthorized() throws Exception {
        mockMvc.perform(post("/championships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(championship1)))
                .andExpect(status().isUnauthorized());

        verify(championshipService, never()).createChampionship(any());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when non-admin user tries to create championship.")
    @WithMockUser(username = "user", roles = "USER")
    void testCreateChampionship_ForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(post("/championships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(championship1)))
                .andExpect(status().isForbidden());

        verify(championshipService, never()).createChampionship(any());
    }

    @Test
    @DisplayName("Should update championship successfully.")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateChampionship_Success() throws Exception {
        var updatedChampionship = ChampionshipMother.championship()
                .withName("Championnat 2024 - Modifié")
                .withDescription("Description modifiée")
                .withStatus(Status.ONGOING)
                .build();

        when(championshipService.updateChampionship(eq(1), any(Championship.class)))
                .thenReturn(updatedChampionship);

        mockMvc.perform(put("/championships/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedChampionship)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(updatedChampionship.getId())))
                .andExpect(jsonPath("$.name", equalTo(updatedChampionship.getName())))
                .andExpect(jsonPath("$.status", equalTo(updatedChampionship.getStatus())));

        verify(championshipService, times(1)).updateChampionship(eq(1), any(Championship.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent championship.")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateChampionship_NotFound() throws Exception {
        var updatedChampionship = ChampionshipMother.championship().withId(999).build();

        when(championshipService.updateChampionship(eq(updatedChampionship.getId()), any(Championship.class)))
                .thenReturn(null);

        mockMvc.perform(put("/championships/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedChampionship)))
                .andExpect(status().isNotFound());

        verify(championshipService, times(1)).updateChampionship(eq(updatedChampionship.getId()), any(Championship.class));
    }

    @DisplayName("Should return 401 Unauthorized when updating championship without authentication.")
    @Test
    void testUpdateChampionship_Unauthorized() throws Exception {
        mockMvc.perform(put("/championships/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(championship1)))
                .andExpect(status().isUnauthorized());

        verify(championshipService, never()).updateChampionship(any(), any());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when non-admin user tries to update championship.")
    @WithMockUser(username = "user", roles = "USER")
    void testUpdateChampionship_ForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(put("/championships/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(championship1)))
                .andExpect(status().isForbidden());

        verify(championshipService, never()).updateChampionship(any(), any());
    }

    @Test
    @DisplayName("Should delete championship successfully.")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteChampionship_Success() throws Exception {
        when(championshipService.getChampionshipById(1)).thenReturn(championship1);
        doNothing().when(championshipService).deleteChampionship(1);

        mockMvc.perform(delete("/championships/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(championshipService, times(1)).getChampionshipById(1);
        verify(championshipService, times(1)).deleteChampionship(1);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent championship.")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteChampionship_NotFound() throws Exception {
        when(championshipService.getChampionshipById(999)).thenReturn(null);

        mockMvc.perform(delete("/championships/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(championshipService, times(1)).getChampionshipById(999);
        verify(championshipService, never()).deleteChampionship(999);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when deleting championship without authentication.")
    void testDeleteChampionship_Unauthorized() throws Exception {
        mockMvc.perform(delete("/championships/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(championshipService, never()).deleteChampionship(any());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when non-admin user tries to delete championship.")
    @WithMockUser(username = "user", roles = "USER")
    void testDeleteChampionship_ForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(delete("/championships/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(championshipService, never()).deleteChampionship(any());
    }
}