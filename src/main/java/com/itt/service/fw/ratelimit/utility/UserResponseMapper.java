package com.itt.service.fw.ratelimit.utility;

import com.itt.service.entity.User;
import com.itt.service.fw.ratelimit.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {
    public UserResponseDto toDto(User user, String planName) {
        return UserResponseDto.builder()
                .id(user.getId())
                .emailId(user.getEmailId())
                .createdAt(user.getCreatedAt())
                .planName(planName)
                .build();
    }
}
