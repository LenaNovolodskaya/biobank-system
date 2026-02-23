package ru.healthfamily.biobank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.healthfamily.biobank.dto.AuthResponse;
import ru.healthfamily.biobank.dto.LoginRequest;
import ru.healthfamily.biobank.dto.RegisterRequest;
import ru.healthfamily.biobank.model.Role;
import ru.healthfamily.biobank.model.User;
import ru.healthfamily.biobank.repository.RoleRepository;
import ru.healthfamily.biobank.repository.UserRepository;
import ru.healthfamily.biobank.security.CustomUserDetails;
import ru.healthfamily.biobank.security.JwtTokenProvider;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        Role viewerRole = roleRepository.findByRoleName("Пользователь")
                .orElseThrow(() -> new IllegalStateException("Роль 'Пользователь' не найдена. Выполните инициализацию БД."));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setIsActive(true);
        user.getRoles().add(viewerRole);

        user = userRepository.save(user);

        Set<String> permissions = viewerRole.getPermissions().stream()
                .map(p -> p.getPermissionName())
                .collect(java.util.stream.Collectors.toSet());

        String token = jwtTokenProvider.generateToken(
                user.getUsername(),
                permissions
        );

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getFullName(),
                user.getUserId(),
                permissions
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Set<String> permissions = userDetails.getPermissionNames();

        String token = jwtTokenProvider.generateToken(
                userDetails.getUsername(),
                permissions
        );

        return new AuthResponse(
                token,
                userDetails.getUsername(),
                userDetails.getFullName(),
                userDetails.getUserId(),
                permissions
        );
    }
}
