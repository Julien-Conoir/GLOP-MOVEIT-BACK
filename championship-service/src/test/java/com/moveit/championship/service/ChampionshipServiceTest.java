package com.moveit.championship.service;

import com.moveit.championship.entity.Championship;
import com.moveit.championship.exception.ChampionshipNotFoundException;
import com.moveit.championship.mother.ChampionshipMother;
import com.moveit.championship.repository.ChampionshipRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChampionshipServiceTest {
    @InjectMocks
    ChampionshipService championshipService;

    @Mock
    ChampionshipRepository championshipRepository;

    @Test
    @DisplayName("Should retrieve all championships.")
    void shouldGetAllChampionships() {
        var championship = ChampionshipMother.championship().build();

        when(championshipRepository.findAll())
                .thenReturn(List.of(championship));

        var championships = championshipService.getAllChampionships();
        assertThat(championships).isEqualTo(List.of(championship));
    }

    @Test
    @DisplayName("Should retrieve championship by id.")
    void shouldGetChampionshipById() {
        var championship = ChampionshipMother.championship().build();

        when(championshipRepository.findById(championship.getId()))
                .thenReturn(Optional.of(championship));

        var result = championshipService.getChampionshipById(championship.getId());
        assertThat(result).isEqualTo(championship);
    }

    @Test
    @DisplayName("Should throw exception when championship not found by id.")
    void shouldThrowExceptionWhenChampionshipNotFound() {
        var championshipId = ChampionshipMother.championship().build().getId();

        when(championshipRepository.findById(championshipId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> championshipService.getChampionshipById(championshipId))
                .isInstanceOf(ChampionshipNotFoundException.class)
                .hasMessageContaining("Championship not found with id: " + championshipId);
    }

    @Test
    @DisplayName("Should create championship.")
    void shouldCreateChampionship() {
        var championship = ChampionshipMother.championship().build();

        when(championshipRepository.save(any(Championship.class)))
                .thenReturn(championship);

        var result = championshipService.createChampionship(championship);

        assertThat(result).isEqualTo(championship);
        verify(championshipRepository, times(1)).save(championship);
    }

    @Test
    @DisplayName("Should update championship.")
    void shouldUpdateChampionship() {
        var existingChampionship = ChampionshipMother.championship().build();
        var updatedChampionship = ChampionshipMother.championship()
                .withName("Updated Championship")
                .build();

        when(championshipRepository.findById(existingChampionship.getId()))
                .thenReturn(Optional.of(existingChampionship));
        when(championshipRepository.save(any(Championship.class)))
                .thenReturn(updatedChampionship);

        var result = championshipService.updateChampionship(existingChampionship.getId(), updatedChampionship);

        assertThat(result).isEqualTo(updatedChampionship);
        verify(championshipRepository, times(1)).save(any(Championship.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent championship.")
    void shouldThrowExceptionWhenUpdatingNonExistentChampionship() {
        var championship = ChampionshipMother.championship().build();
        var championshipId = championship.getId();

        when(championshipRepository.findById(championshipId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> championshipService.updateChampionship(championshipId, championship))
                .isInstanceOf(ChampionshipNotFoundException.class)
                .hasMessageContaining("Championship not found with id: " + championshipId);

        verify(championshipRepository, never()).save(any(Championship.class));
    }

    @Test
    @DisplayName("Should delete championship.")
    void shouldDeleteChampionship() {
        var championshipId = ChampionshipMother.championship().build().getId();

        when(championshipRepository.existsById(championshipId))
                .thenReturn(true);
        doNothing().when(championshipRepository).deleteById(championshipId);

        championshipService.deleteChampionship(championshipId);

        verify(championshipRepository, times(1)).deleteById(championshipId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent championship.")
    void shouldThrowExceptionWhenDeletingNonExistentChampionship() {
        var championshipId = ChampionshipMother.championship().build().getId();

        when(championshipRepository.existsById(championshipId))
                .thenReturn(false);

        assertThatThrownBy(() -> championshipService.deleteChampionship(championshipId))
                .isInstanceOf(ChampionshipNotFoundException.class)
                .hasMessageContaining("Championship not found with id: " + championshipId);

        verify(championshipRepository, never()).deleteById(championshipId);
    }
}