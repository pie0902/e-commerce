package org.example.tentrilliondollars.user.dto;

import lombok.Getter;
import org.example.tentrilliondollars.user.entity.UserRoleEnum;

@Getter
public class UserResponseDto {

    private String username;
    private String email;
    private UserRoleEnum role;

    public UserResponseDto(String username, String email, UserRoleEnum role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
