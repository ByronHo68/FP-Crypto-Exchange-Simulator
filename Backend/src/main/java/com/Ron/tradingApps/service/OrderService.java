package com.Ron.tradingApps.service;

import com.Ron.tradingApps.dto.OrderDTO;
import com.Ron.tradingApps.dto.request.OrderRequestDTO;
import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.dto.response.TransactionResponseDTO;
import com.Ron.tradingApps.dto.response.WalletWebsocketDTO;
import com.Ron.tradingApps.mapper.OrderMapper;
import com.Ron.tradingApps.model.Order;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.model.Transaction;
import com.Ron.tradingApps.model.Wallet;
import com.Ron.tradingApps.repository.OrderRepository;
import com.Ron.tradingApps.repository.TraderRepository;
import com.Ron.tradingApps.repository.WalletRepository;
import com.Ron.tradingApps.service.data.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TraderRepository traderRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    @Lazy
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private PriceService priceService;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        Trader trader = traderRepository.findById(requestDTO.getTraderId())
                .orElseThrow(() -> new IllegalArgumentException("Trader not found"));

        Order order = Order.builder()
                .trader(trader)
                .marketOrLimitOrderTypes(requestDTO.getMarketOrLimitOrderTypes())
                .price(requestDTO.getPrice())
                .amount(requestDTO.getAmount())
                .buyAndSellType(requestDTO.getBuyAndSellType())
                .currency(requestDTO.getCurrency())
                .orderStatus("Pending")
                .build();

        order = orderRepository.save(order);
        String userId = trader.getUserId();

        if ("buy".equalsIgnoreCase(order.getBuyAndSellType())) {
            BigDecimal cost = order.getPrice().multiply(order.getAmount());
            Wallet usdtWallet = walletService.findOrCreateWallet(trader, "USDT");

            if (usdtWallet.getAmount().compareTo(cost) < 0) {
                throw new IllegalArgumentException("Insufficient USDT balance");
            }

            usdtWallet.setAmount(usdtWallet.getAmount().subtract(cost));
            walletRepository.save(usdtWallet);

            WalletWebsocketDTO usdtWalletDTO = new WalletWebsocketDTO(
                    usdtWallet.getId(),
                    trader.getId(),
                    "USDT",
                    usdtWallet.getAmount()
            );

            messagingTemplate.convertAndSend("/topic/wallets/" + userId, usdtWalletDTO);

        } else if ("sell".equalsIgnoreCase(order.getBuyAndSellType())) {
            Wallet currencyWallet = walletService.findOrCreateWallet(trader, order.getCurrency());

            if (currencyWallet.getAmount().compareTo(order.getAmount()) < 0) {
                throw new IllegalArgumentException("Insufficient currency balance");
            }

            currencyWallet.setAmount(currencyWallet.getAmount().subtract(order.getAmount()));
            walletRepository.save(currencyWallet);

            WalletWebsocketDTO currencyWalletDTO = new WalletWebsocketDTO(
                    currencyWallet.getId(),
                    trader.getId(),
                    order.getCurrency(),
                    currencyWallet.getAmount()
            );

            messagingTemplate.convertAndSend("/topic/wallets/" + userId, currencyWalletDTO);
        }
        if("limit".equalsIgnoreCase(order.getMarketOrLimitOrderTypes())) {
            String currency = order.getCurrency();

            if (!currency.equals("ETHUSDT") && !currency.equals("BTCUSDT")) {
                throw new IllegalArgumentException("Unsupported currency: " + currency);
            }

            Double latestPrice = priceService.getLatestPrice(currency);

            if (latestPrice != null) {
                BigDecimal latestPriceBD = BigDecimal.valueOf(latestPrice);
                BigDecimal orderPrice = order.getPrice();

                BigDecimal onePercentDifference = latestPriceBD.multiply(BigDecimal.valueOf(0.01));
                BigDecimal lowerBound = latestPriceBD.subtract(onePercentDifference);
                BigDecimal upperBound = latestPriceBD.add(onePercentDifference);

                if (orderPrice.compareTo(lowerBound) < 0 || orderPrice.compareTo(upperBound) > 0) {
                    OrderDTO pendingOrderDto = new OrderDTO(
                            order.getId(),
                            trader.getId(),
                            order.getPrice(),
                            order.getAmount(),
                            order.getBuyAndSellType(),
                            order.getCurrency(),
                            order.getMarketOrLimitOrderTypes(),
                            order.getOrderStatus()
                    );
                    messagingTemplate.convertAndSend("/topic/orders/pending/" + userId, pendingOrderDto);
                }
            }
        }
        return OrderMapper.toResponseDTO(order);
    }

    public Order findById(Integer orderId) throws ResourceNotFoundException {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found by id " + orderId
                ));
    }

    @Transactional
    public void deleteOrder(Integer orderId) throws ResourceNotFoundException {
        log.info("Fetching order with ID: {}", orderId);
        Order existingOrder = findById(orderId);

        if (existingOrder == null) {
            log.error("Order with ID {} not found.", orderId);
            throw new ResourceNotFoundException("Order not found");
        }

        Trader trader = existingOrder.getTrader();
        log.info("Deleting order for trader: {}", trader.getUserId());
        String userId = trader.getUserId();

        Wallet walletToUpdate = null;

        if ("buy".equalsIgnoreCase(existingOrder.getBuyAndSellType())) {
            walletToUpdate = walletService.findOrCreateWallet(trader, "USDT");
            walletToUpdate.setAmount(walletToUpdate.getAmount().add(existingOrder.getAmount()));
            log.info("Updated USDT wallet for trader {}: new amount is {}", userId, walletToUpdate.getAmount());
        } else if ("sell".equalsIgnoreCase(existingOrder.getBuyAndSellType())) {
            walletToUpdate = walletService.findOrCreateWallet(trader, existingOrder.getCurrency());
            walletToUpdate.setAmount(walletToUpdate.getAmount().add(existingOrder.getAmount()));
            log.info("Updated {} wallet for trader {}: new amount is {}", existingOrder.getCurrency(), userId, walletToUpdate.getAmount());
        }

        if (walletToUpdate != null) {
            walletRepository.save(walletToUpdate);

            WalletWebsocketDTO walletDTO = new WalletWebsocketDTO(
                    walletToUpdate.getId(),
                    trader.getId(),
                    walletToUpdate.getCurrency(),
                    walletToUpdate.getAmount()
            );

            messagingTemplate.convertAndSend("/topic/wallets/" + userId, walletDTO);
        } else {
            log.error("No wallet was updated for order ID: {}", orderId);
        }
        messagingTemplate.convertAndSend("/topic/orders/pending/" + userId,
                new OrderDTO(existingOrder.getId(), trader.getId(), null, null, null, null, null, "deleted"));

        log.info("Deleting existing order with ID: {}", existingOrder.getId());
        orderRepository.delete(existingOrder);
    }

    public List<OrderResponseDTO> findAllOrdersByTrader(String userId) {
        Optional<Trader> trader = traderRepository.findByUserId(userId);
        int traderId = trader.get().getId();
        List<Order> orders = orderRepository.findAllByTraderId(traderId);
        return OrderMapper.toResponseDTOList(orders);
    }

    public List<OrderResponseDTO> findPendingOrdersByTrader(String userId) {
        Optional<Trader> trader = traderRepository.findByUserId(userId);
        int traderId = trader.get().getId();
        List<Order> pendingOrders = orderRepository.findPendingOrdersByTraderId(traderId);
        return pendingOrders.stream()
                .map(order -> OrderResponseDTO.builder()
                        .id(order.getId())
                        .traderId(order.getTrader().getId())
                        .marketOrLimitOrderTypes(order.getMarketOrLimitOrderTypes())
                        .buyAndSellType(order.getBuyAndSellType())
                        .currency(order.getCurrency())
                        .price(order.getPrice())
                        .amount(order.getAmount())
                        .orderStatus(order.getOrderStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getAllTransactions() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }
}