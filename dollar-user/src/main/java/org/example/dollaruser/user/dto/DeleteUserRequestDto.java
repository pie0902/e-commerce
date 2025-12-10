package org.example.dollaruser.user.dto;

import lombok.Getter;

@Getter
public class DeleteUserRequestDto {

    private String password;

    private String passwordCheck;
}
