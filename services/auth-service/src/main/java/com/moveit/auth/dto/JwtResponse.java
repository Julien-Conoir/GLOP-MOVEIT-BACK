package com.moveit.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private UserDto user;

    public JwtResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }
}