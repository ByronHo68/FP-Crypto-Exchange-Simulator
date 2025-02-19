package com.Ron.tradingApps.controller.websocket;

import com.Ron.tradingApps.dto.CandleDTO;
import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.dto.response.WalletDResponseDTO;
import com.Ron.tradingApps.service.data.CandleProviderService;
import com.Ron.tradingApps.service.order.OrderService;
import com.Ron.tradingApps.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
public class UnifiedWebSocketController {

    @Autowired
    private CandleProviderService candleProviderService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/candles")
    @SendTo("/topic/candles")
    public List<CandleDTO> fetchCandles(String symbol) throws Exception {
        log.info("Fetching candles for symbol: {}", symbol);
        List<CandleDTO> candles = candleProviderService.fetchCandlesByTime(symbol);
        messagingTemplate.convertAndSend("/topic/candles/" + symbol, candles);
        return candles;
    }


    @MessageMapping("/wallets/update/{userId}")
    @SendTo("/topic/wallets/{userId}")
    public List<WalletDResponseDTO> updateWallets(@DestinationVariable String userId) {
        log.info("Received request for wallets for user ID: {}", userId);
        List<WalletDResponseDTO> updatedWallets = walletService.getWalletsForTrader(userId);
        log.info("Sending {} wallets for user ID: {}", updatedWallets.size(), userId);
        messagingTemplate.convertAndSend("/topic/wallets/" + userId, updatedWallets);
        return updatedWallets;
    }


    @MessageMapping("/orders/all/{userId}")
    @SendTo("/topic/orders/all/{userId}")
    public List<OrderResponseDTO> getAllOrders(@DestinationVariable String userId) {
        log.info("Received request for orders for user ID: {}", userId);
        List<OrderResponseDTO> allOrders = orderService.findAllOrdersByTrader(userId);
        log.info("Sending {} orders for user ID: {}", allOrders.size(), userId);
        messagingTemplate.convertAndSend("/topic/orders/all/" + userId, allOrders);
        return allOrders;
    }


    @MessageMapping("/orders/pending/{userId}")
    @SendTo("/topic/orders/pending/{userId}")
    public List<OrderResponseDTO> getPendingOrders(@DestinationVariable String userId) {
        log.info("Received request for pending orders for user ID: {}", userId);
        List<OrderResponseDTO> pendingOrders = orderService.findPendingOrdersByTrader(userId);
        log.info("Sending {} pending orders for user ID: {}", pendingOrders.size(), userId);
        messagingTemplate.convertAndSend("/topic/orders/pending/" + userId, pendingOrders);
        return pendingOrders;
    }
}