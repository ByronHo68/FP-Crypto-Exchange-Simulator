package com.Ron.tradingApps.mapper;

import com.Ron.tradingApps.dto.request.OrderRequestDTO;
import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.model.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class OrderMapper {

    public static Order toEntity(OrderRequestDTO dto) {
        Order order = new Order();
        order.setMarketOrLimitOrderTypes(dto.getMarketOrLimitOrderTypes());
        order.setBuyAndSellType(dto.getBuyAndSellType());
        order.setCurrency(dto.getCurrency());
        order.setPrice(dto.getPrice());
        order.setAmount(dto.getAmount());
        return order;
    }

    public static OrderResponseDTO toResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .traderId(order.getTrader().getId())
                .marketOrLimitOrderTypes(order.getMarketOrLimitOrderTypes())
                .buyAndSellType(order.getBuyAndSellType())
                .currency(order.getCurrency())
                .price(order.getPrice())
                .amount(order.getAmount())
                .orderStatus(order.getOrderStatus())
                .updateTime(LocalDateTime.now())
                .build();
    }

    public static List<OrderResponseDTO> toResponseDTOList(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO toDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .traderId(order.getTrader().getId())
                .marketOrLimitOrderTypes(order.getMarketOrLimitOrderTypes())
                .buyAndSellType(order.getBuyAndSellType())
                .currency(order.getCurrency())
                .price(order.getPrice())
                .amount(order.getAmount())
                .orderStatus(order.getOrderStatus())
                .updateTime(LocalDateTime.now())
                .build();
    }

}