package com.Ron.tradingApps.mapper;

import com.Ron.tradingApps.dto.request.InstructorRequestDTO;
import com.Ron.tradingApps.dto.request.TraderRequestDTO;
import com.Ron.tradingApps.dto.response.InstructorResponseDTO;
import com.Ron.tradingApps.dto.response.TraderResponseDTO;
import com.Ron.tradingApps.model.Instructor;
import com.Ron.tradingApps.model.Trader;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {
    public Instructor toEntity(InstructorRequestDTO requestDTO) {
        return Instructor.builder()
                .username(requestDTO.getUsername())
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword())
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .createdAt(requestDTO.getCreatedAt())
                .updatedAt(requestDTO.getUpdatedAt())
                .userId(requestDTO.getUserId())
                .instructorNumber(requestDTO.getInstructorNumber())
                .build();
    }

    public InstructorResponseDTO toDTO(Instructor instructor) {
        return InstructorResponseDTO.builder()
                .id(instructor.getId())
                .username(instructor.getUsername())
                .email(instructor.getEmail())
                .password(instructor.getPassword())
                .firstName(instructor.getFirstName())
                .lastName(instructor.getLastName())
                .createdAt(instructor.getCreatedAt())
                .updatedAt(instructor.getUpdatedAt())
                .userId(instructor.getUserId())
                .instructorNumber(instructor.getInstructorNumber())
                .build();

    }
}
