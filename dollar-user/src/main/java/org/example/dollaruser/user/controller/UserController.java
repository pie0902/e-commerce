package org.example.dollaruser.user.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dollaruser.user.dto.DeleteUserRequestDto;
import org.example.dollaruser.user.dto.LoginRequestDto;
import org.example.dollaruser.user.dto.ModifyPasswordRequestDto;
import org.example.dollaruser.user.dto.ModifyUserNameRequestDto;
import org.example.dollaruser.user.dto.SignupRequestDto;
import org.example.dollaruser.user.dto.UserResponseDto;
import org.example.dollaruser.user.service.UserService;
import org.example.share.config.global.entity.user.User;
import org.example.share.config.global.jwt.JwtUtil;
import org.example.share.config.global.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;


    @PostMapping("/signup")
    public ResponseEntity<String> signup(
        @Valid @RequestBody SignupRequestDto signupRequestDto) {
        userService.signup(signupRequestDto);
        return ResponseEntity.ok().body("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto,
        HttpServletResponse response) {
        User loginedUser = userService.login(loginRequestDto);
        String token = jwtUtil.createToken(loginedUser.getId(), loginedUser.getEmail(),
            loginedUser.getUsername(), loginedUser.getRole());
        response.setHeader(JwtUtil.AUTHORIZATION_HEADER, token);
        jwtUtil.addJwtToCookie(token, response);

        // JwtUtil.addJwtToCookie 만 사용 (중복 Set-Cookie 방지)

        return ResponseEntity.ok().body(loginedUser.getRole().toString());
    }


    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> showUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        UserResponseDto userResponseDto = userService.showUser(userDetails.getUser());
        return ResponseEntity.ok().body(userResponseDto);
    }


    @PutMapping("/username")
    public ResponseEntity<String> modifyUsername(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Valid @RequestBody
        ModifyUserNameRequestDto modifyUserNameRequestDto) {
        userService.modifyUsername(userDetails.getUser(), modifyUserNameRequestDto);

        return ResponseEntity.ok().body("유저네임 수정 성공");
    }

    @PutMapping("/password")
    public ResponseEntity<String> modifyPassword(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Valid @RequestBody
        ModifyPasswordRequestDto modifyPasswordRequestDto) {
        userService.modifyPassword(userDetails.getUser(), modifyPasswordRequestDto);

        return ResponseEntity.ok().body("비밀번호 수정 성공");
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody DeleteUserRequestDto deleteUserRequestDto) {
        userService.deleteUser(userDetails.getUser(), deleteUserRequestDto);

        return ResponseEntity.ok().body("회원 탈퇴 성공");
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        jwtUtil.removeJwtAtCookie(response);
        return ResponseEntity.ok().body("로그아웃 성공");
    }
}
