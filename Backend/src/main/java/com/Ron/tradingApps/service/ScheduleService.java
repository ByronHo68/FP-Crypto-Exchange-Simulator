package com.Ron.tradingApps.service;

import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.mapper.OrderMapper;
import com.Ron.tradingApps.model.Order;
import com.Ron.tradingApps.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ScheduleService {

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private PriceCheckService priceCheckService;
        @Autowired
        private OrderMapper orderMapper;

        @Scheduled(fixedRate = 6000, initialDelay = 19000)
        public void checkPendingOrders() {
                List<Order> pendingOrders = orderRepository.findAllPendingOrders();

                for (Order order : pendingOrders) {
                        OrderResponseDTO orderResponseDTO = orderMapper.toResponseDTO(order);

                        try {
                                priceCheckService.checkAndExecuteTransaction(orderResponseDTO);
                                /*System.out.println("Order checked for order ID: " + order.getId());*/
                        } catch (Exception e) {
                                System.out.println("Error executing transaction for order ID: " + order.getId() + " - " + e.getMessage());
                        }
                }
        }
}
