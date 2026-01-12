package com.moveit.championship.exception;

public class ChampionshipNotFoundException extends RuntimeException {
    public ChampionshipNotFoundException(Integer id) {
        super("Championship with id " + id + " not found");
    }
}