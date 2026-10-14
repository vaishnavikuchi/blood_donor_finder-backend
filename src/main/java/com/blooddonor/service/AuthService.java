package com.blooddonor.service;

import com.blooddonor.config.JwtUtils;
import com.blooddonor.dto.AuthRequest;
import com.blooddonor.dto.AuthResponse;
import com.blooddonor.dto.RegisterRequest;
import com.blooddonor.dto.UserDto;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.User;
import com.blooddonor.exception.ConflictException;
import com.blooddonor.exception.UnauthorizedException;
import com.blooddonor.repository.DonorProfileRepository;
import com.blooddonor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("Email already in use: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail(), user.getId());
        UserDto userDto = convertToDto(user, null);

        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getId());
        Optional<DonorProfile> donorProfile = donorProfileRepository.findByUserId(user.getId());
        UserDto userDto = convertToDto(user, donorProfile.orElse(null));

        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUserDto(User user) {
        Optional<DonorProfile> donorProfile = donorProfileRepository.findByUserId(user.getId());
        return convertToDto(user, donorProfile.orElse(null));
    }

    public UserDto convertToDto(User user, DonorProfile donorProfile) {
        boolean hasProfile = donorProfile != null;
        Long profileId = hasProfile ? donorProfile.getId() : null;

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .hasDonorProfile(hasProfile)
                .donorProfileId(profileId)
                .build();
    }
}
