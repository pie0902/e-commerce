package org.example.tentrilliondollars.user.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.tentrilliondollars.global.exception.BadRequestException;
import org.example.tentrilliondollars.global.exception.ConflictException;
import org.example.tentrilliondollars.global.exception.NotFoundException;
import org.example.tentrilliondollars.global.exception.UnauthorizedAccessException;
import org.example.tentrilliondollars.user.dto.DeleteUserRequestDto;
import org.example.tentrilliondollars.user.dto.LoginRequestDto;
import org.example.tentrilliondollars.user.dto.ModifyPasswordRequestDto;
import org.example.tentrilliondollars.user.dto.ModifyUserNameRequestDto;
import org.example.tentrilliondollars.user.dto.SignupRequestDto;
import org.example.tentrilliondollars.user.dto.UserResponseDto;
import org.example.tentrilliondollars.user.entity.User;
import org.example.tentrilliondollars.user.entity.UserRoleEnum;
import org.example.tentrilliondollars.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    private final String SELLER_TOKEN = "AAABnvxRVklrnYxKZaHgTBcXukeZygoC";


    public void signup(SignupRequestDto signupRequestDto) {
        String username = signupRequestDto.getUsername();
        String email = signupRequestDto.getEmail();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());

        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new ConflictException("중복된 username입니다.");
        }

        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new ConflictException("중복된 email입니다.");
        }

        UserRoleEnum role = UserRoleEnum.USER;
        if (signupRequestDto.isAdmin()) {
            if (!SELLER_TOKEN.equals(signupRequestDto.getAdminToken())) {
                throw new UnauthorizedAccessException("관리자 암호가 일치하지 않습니다.");
            }
            role = UserRoleEnum.SELLER;
        }

        userRepository.save(User.builder()
            .email(email)
            .username(username)
            .password(password)
            .role(role)
            .build());
    }

    public User login(LoginRequestDto loginRequestDto) {

        User user = userRepository.findByEmail(loginRequestDto.getEmail())
            .orElseThrow(() -> new NotFoundException("존재하지 않는 계정입니다."));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedAccessException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public UserResponseDto showUser(User user) {
        User showUser = userRepository.findById(user.getId()).orElseThrow();
        return new UserResponseDto(showUser.getUsername(), showUser.getEmail(), showUser.getRole());
    }


    @Transactional
    public void modifyUsername(User user, ModifyUserNameRequestDto modifyUserNameRequestDto) {
        User changeNameUser = userRepository.findById(user.getId()).orElseThrow();

        changeNameUser.modifyUsername(modifyUserNameRequestDto.getUsername());
    }

    @Transactional
    public void modifyPassword(User user, ModifyPasswordRequestDto modifyPasswordRequestDto) {
        User changePasswordUser = userRepository.findById(user.getId()).orElseThrow();

        if (!passwordEncoder.matches(modifyPasswordRequestDto.getPassword(),
            changePasswordUser.getPassword())) {
            throw new UnauthorizedAccessException("비밀번호 불일치");
        }

        if (!modifyPasswordRequestDto.getChangePassword()
            .equals(modifyPasswordRequestDto.getChangePasswordCheck())) {
            throw new BadRequestException("변경할 비밀번호 확인");
        }

        String changedPassword = passwordEncoder.encode(
            modifyPasswordRequestDto.getChangePasswordCheck());

        changePasswordUser.modifyPassword(changedPassword);
    }

    public void deleteUser(User user, DeleteUserRequestDto deleteUserRequestDto) {
        User deleteUser = userRepository.findById(user.getId()).orElseThrow();

        if (!passwordEncoder.matches(deleteUserRequestDto.getPassword(),
            deleteUser.getPassword())) {
            throw new UnauthorizedAccessException("비밀번호가 일치하지 않습니다.");
        }

        if (!deleteUserRequestDto.getPassword().equals(deleteUserRequestDto.getPasswordCheck())) {
            throw new BadRequestException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.deleteById(user.getId());
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }


}
