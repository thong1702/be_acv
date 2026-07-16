package com.example.acv.service;

import com.example.acv.dto.request.LoginRequest;
import com.example.acv.dto.request.RefreshRequest;
import com.example.acv.dto.response.AuthResponse;
import com.example.acv.dto.response.UserResponse;
import com.example.acv.entity.User;
import com.example.acv.exception.AuthException;
import com.example.acv.repository.UserRepository;
import com.example.acv.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (user.getStatus() != 1) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        return new AuthResponse(token, refreshToken, user.getUsername(), user.getRole());
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Người dùng không tồn tại"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request, String authHeader) {
        String token = null;
        if (request != null && request.getToken() != null) {
            token = request.getToken();
        }
        if ((token == null || token.isEmpty()) && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null || token.isEmpty()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Token không được để trống");
        }

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "Refresh Token không hợp lệ hoặc đã hết hạn");
            }

            String username = jwtTokenProvider.getUsernameFromJWT(token);

            User user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Người dùng không tồn tại"));

            if (user.getStatus() != 1) {
                throw new AuthException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
            }

            String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
            return new AuthResponse(newAccessToken, newRefreshToken, user.getUsername(), user.getRole());
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token không hợp lệ");
        }
    }
}
