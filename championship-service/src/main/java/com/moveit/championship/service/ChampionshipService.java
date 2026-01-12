package com.moveit.championship.service;

import com.moveit.championship.entity.Championship;
import com.moveit.championship.exception.ChampionshipNotFoundException;
import com.moveit.championship.repository.ChampionshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChampionshipService {

    private final ChampionshipRepository championshipRepository;

    public List<Championship> getAllChampionships() {
        return championshipRepository.findAll();
    }

    public Championship getChampionshipById(Integer id) {
        return championshipRepository.findById(id)
                .orElseThrow(() -> new ChampionshipNotFoundException(id));
    }

    public Championship createChampionship(Championship championship) {
        return championshipRepository.save(championship);
    }

    public Championship updateChampionship(Integer id, Championship championship) {
        championshipRepository.findById(id)
                .orElseThrow(() -> new ChampionshipNotFoundException(id));
        championship.setId(id);
        return championshipRepository.save(championship);
    }

    public void deleteChampionship(Integer id) {
        if (!championshipRepository.existsById(id)) {
            throw new ChampionshipNotFoundException(id);
        }
        championshipRepository.deleteById(id);
    }
}