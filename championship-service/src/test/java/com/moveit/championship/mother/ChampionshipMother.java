package com.moveit.championship.mother;

import com.moveit.championship.entity.Championship;
import com.moveit.championship.entity.Competition;
import com.moveit.championship.entity.Status;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChampionshipMother {
    public static Builder championship() {
        return new Builder();
    }

    @With
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Builder {
        private Integer id = 1;
        private List<Competition> competitions = List.of();
        private String name = "Championship Name";
        private String description = "Championship Description";
        private Date startDate = new Date(2026, Calendar.JANUARY,1);
        private Date endDate = new Date(2026, Calendar.APRIL,2);
        private Status status = Status.PLANNED;

        public Championship build() {
            return new Championship(
                    id,
                    competitions,
                    name,
                    description,
                    startDate,
                    endDate,
                    status
            );
        }
    }
}