package com.Ron.tradingApps.service.user;

import com.Ron.tradingApps.dto.request.OrderRequestDTO;
import com.Ron.tradingApps.model.Order;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.repository.OrderRepository;
import com.Ron.tradingApps.repository.TraderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsernameCheckerService {

    @Autowired
    private TraderRepository traderRepository;
    @Autowired
    private OrderRepository orderRepository;

    public boolean checkUsername(String username, OrderRequestDTO orderRequestDTO){

        Trader trader = traderRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trader not found by id " + username
                ));
        int traderId = orderRequestDTO.getTraderId();
        return trader.getId().equals(traderId);
    }

    public boolean checkOrderId(String username, int orderId){

        Trader trader = traderRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found by id " + username
                ));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException(
                "Order not found by id " + orderId
        ));
        return trader.getId().equals(order.getTrader().getId());
    }

    public boolean checkUid(String username, String userId){

        Trader trader = traderRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trader not found by id " + username
                ));

        Trader traderDataBase = traderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trader not found by uid " + userId
                ));
        return trader.getId().equals(traderDataBase.getId());
    }
}
