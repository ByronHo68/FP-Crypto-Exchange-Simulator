package com.Ron.tradingApps.controller;

import com.Ron.tradingApps.dto.request.OrderRequestDTO;
import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.service.wallet.BalanceCheckService;
import com.Ron.tradingApps.service.order.OrderService;
import com.Ron.tradingApps.service.order.PriceCheckService;
import com.Ron.tradingApps.service.user.UsernameCheckerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    @Autowired
    private UsernameCheckerService usernameCheckerService;


    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO, Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String username = token.getTokenAttributes().get("name").toString();

        boolean isUsernameValid = usernameCheckerService.checkUsername(username, orderRequestDTO);

        if (!isUsernameValid) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

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
    public ResponseEntity<Void> deleteOrder(@PathVariable int orderId, Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String username = token.getTokenAttributes().get("name").toString();

        boolean isUsernameValid = usernameCheckerService.checkOrderId(username, orderId);

        if (!isUsernameValid) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

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
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersForTrader(@PathVariable String userId, Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String username = token.getTokenAttributes().get("name").toString();

        boolean isUsernameValid = usernameCheckerService.checkUid(username, userId);

        if (!isUsernameValid) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        try {
            List<OrderResponseDTO> orders = orderService.findAllOrdersByTrader(userId);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trader/{userId}/pending")
    public ResponseEntity<List<OrderResponseDTO>> getPendingOrdersForTrader(@PathVariable String userId, Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String username = token.getTokenAttributes().get("name").toString();

        boolean isUsernameValid = usernameCheckerService.checkUid(username, userId);
        try {
            List<OrderResponseDTO> pendingOrders = orderService.findPendingOrdersByTrader(userId);
            return new ResponseEntity<>(pendingOrders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}