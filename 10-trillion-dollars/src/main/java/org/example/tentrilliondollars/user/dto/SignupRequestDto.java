package org.example.tentrilliondollars.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupRequestDto {

    @NotEmpty
    @Email(message = "email형식 이여야 합니다.")
    private String email;

    @NotEmpty(message = " username은 필수 입력 값입니다.")
//    @Pattern(regexp = "^[a-zA-z0-9]{4,10}$", message = "최소 4자 이상, 10자 이하이며 알파벳 소문자(a~z), 숫자(0~9)")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요.")
//    @Min(8)
    private String password;

    private boolean admin = false;
    private String adminToken = "";
}
