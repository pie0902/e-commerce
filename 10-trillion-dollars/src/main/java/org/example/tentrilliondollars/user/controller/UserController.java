package org.example.tentrilliondollars.user.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tentrilliondollars.global.jwt.JwtUtil;
import org.example.tentrilliondollars.global.security.UserDetailsImpl;
import org.example.tentrilliondollars.user.dto.DeleteUserRequestDto;
import org.example.tentrilliondollars.user.dto.LoginRequestDto;
import org.example.tentrilliondollars.user.dto.ModifyPasswordRequestDto;
import org.example.tentrilliondollars.user.dto.ModifyUserNameRequestDto;
import org.example.tentrilliondollars.user.dto.SignupRequestDto;
import org.example.tentrilliondollars.user.dto.UserResponseDto;
import org.example.tentrilliondollars.user.entity.User;
import org.example.tentrilliondollars.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        return ResponseEntity.ok().body(loginedUser.getRole().toString());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> showUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
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
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        jwtUtil.removeJwtAtCookie(request, response);
        return ResponseEntity.ok().body("로그아웃 성공");
    }
}
