package com.Ron.tradingApps.mapper;

import com.Ron.tradingApps.dto.request.TraderRequestDTO;
import com.Ron.tradingApps.dto.response.TraderResponseDTO;
import com.Ron.tradingApps.model.Trader;
import org.springframework.stereotype.Component;

@Component
public class TraderMapper {
    public Trader toEntity(TraderRequestDTO requestDTO) {
        return Trader.builder()
                .username(requestDTO.getUsername())
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword())
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .createdAt(requestDTO.getCreatedAt())
                .updatedAt(requestDTO.getUpdatedAt())
                .userId(requestDTO.getUserId())
                .usdtBalance(requestDTO.getUsdtBalance())
                .idNumber(requestDTO.getIdNumber())
                .phoneNumber(requestDTO.getPhoneNumber())
                .yesterdayPrice(requestDTO.getYesterdayPrice())
                .build();
    }

    public TraderResponseDTO toDTO(Trader trader) {
        return TraderResponseDTO.builder()
                .id(trader.getId())
                .username(trader.getUsername())
                .email(trader.getEmail())
                .password(trader.getPassword())
                .firstName(trader.getFirstName())
                .lastName(trader.getLastName())
                .createdAt(trader.getCreatedAt())
                .updatedAt(trader.getUpdatedAt())
                .userId(trader.getUserId())
                .usdtBalance(trader.getUsdtBalance())
                .idNumber(trader.getIdNumber())
                .phoneNumber(trader.getPhoneNumber())
                .yesterdayPrice(trader.getYesterdayPrice())
                .build();

    }
}
