package org.example.tentrilliondollars.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ModifyUserNameRequestDto {

    @NotEmpty(message = " username은 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-z0-9]{4,10}$", message = "최소 4자 이상, 10자 이하이며 알파벳 소문자(a~z), 숫자(0~9)")
    private String username;
}
