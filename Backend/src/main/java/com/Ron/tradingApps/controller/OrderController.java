package com.Ron.tradingApps.controller;

import com.Ron.tradingApps.dto.request.OrderRequestDTO;
import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.service.BalanceCheckService;
import com.Ron.tradingApps.service.OrderService;
import com.Ron.tradingApps.service.PriceCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private BalanceCheckService balanceCheckService;
    @Autowired
    private PriceCheckService priceCheckService;


    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        try {
            OrderResponseDTO orderResponse = orderService.createOrder(orderRequestDTO);
            priceCheckService.checkAndExecuteTransaction(orderResponse);
            return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error creating order", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable int orderId) {
        try {
            log.info("Attempting to delete order with ID: {}", orderId);
            orderService.deleteOrder(orderId);
            log.info("Successfully deleted order with ID: {}", orderId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Order with ID {} not found.", orderId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error deleting order with ID: {}", orderId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trader/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersForTrader(@PathVariable String userId) {
        try {
            List<OrderResponseDTO> orders = orderService.findAllOrdersByTrader(userId);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trader/{userId}/pending")
    public ResponseEntity<List<OrderResponseDTO>> getPendingOrdersForTrader(@PathVariable String userId) {
        try {
            List<OrderResponseDTO> pendingOrders = orderService.findPendingOrdersByTrader(userId);
            return new ResponseEntity<>(pendingOrders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}