package com.Ron.tradingApps.service.order;

import com.Ron.tradingApps.dto.OrderDTO;
import com.Ron.tradingApps.dto.request.OrderRequestDTO;
import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.dto.response.WalletWebsocketDTO;
import com.Ron.tradingApps.mapper.OrderMapper;
import com.Ron.tradingApps.model.*;
import com.Ron.tradingApps.model.type.BuyAndSell;
import com.Ron.tradingApps.model.type.Cryptocurrencies;
import com.Ron.tradingApps.model.type.MarketAndLimit;
import com.Ron.tradingApps.repository.OrderRepository;
import com.Ron.tradingApps.repository.TraderRepository;
import com.Ron.tradingApps.repository.WalletRepository;
import com.Ron.tradingApps.service.data.PriceService;
import com.Ron.tradingApps.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
    private static final String CURRENCY_USDT = "USDT";


    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        Trader trader = getTraderById(requestDTO.getTraderId());
        Order order = buildInitialOrder(trader, requestDTO);

        order = orderRepository.save(order);

        processOrderFunds(trader, order);
        handleLimitOrder(trader, order);
        // pls add the code here

        return OrderMapper.toResponseDTO(order);
    }

    private boolean isOrderPriceWithinBounds(BigDecimal orderPrice, BigDecimal latestPriceBD) {
        BigDecimal onePercentDifference = latestPriceBD.multiply(BigDecimal.valueOf(0.01));
        BigDecimal lowerBound = latestPriceBD.subtract(onePercentDifference);
        BigDecimal upperBound = latestPriceBD.add(onePercentDifference);

        return !(orderPrice.compareTo(lowerBound) < 0 || orderPrice.compareTo(upperBound) > 0);
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

        if (String.valueOf(BuyAndSell.Type.Buy).equalsIgnoreCase(existingOrder.getBuyAndSellType())) {
            walletToUpdate = walletService.findOrCreateWallet(trader, CURRENCY_USDT);
            walletToUpdate.setAmount(walletToUpdate.getAmount().add(existingOrder.getAmount()));
            log.info("Updated USDT wallet for trader {}: new amount is {}", userId, walletToUpdate.getAmount());
        } else if (String.valueOf(BuyAndSell.Type.Sell).equalsIgnoreCase(existingOrder.getBuyAndSellType())) {
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
        Trader trader = traderRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException(
                "Trader not found by id " + userId
        ));
        List<Order> orders = orderRepository.findAllByTraderId(trader.getId());
        return OrderMapper.toResponseDTOList(orders);
    }

    public List<OrderResponseDTO> findPendingOrdersByTrader(String userId) {
        Trader trader = traderRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException(
                "Trader not found by id " + userId
        ));
        List<Order> pendingOrders = orderRepository.findPendingOrdersByTraderId(trader.getId());
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

    private void handleLimitOrder(Trader trader, Order order) {
        if (!isLimitOrder(order)) return;

        validateSupportedCurrency(order.getCurrency());
        Double latestPrice = priceService.getLatestPrice(order.getCurrency());

        if (latestPrice != null && !isOrderPriceWithinBounds(order.getPrice(), BigDecimal.valueOf(latestPrice))) {
            notifyPendingOrder(trader, order);
        }
    }

    private void notifyPendingOrder(Trader trader, Order order) {
        OrderResponseDTO pendingOrderDto = orderMapper.toDTO(order);
        messagingTemplate.convertAndSend("/topic/orders/pending/" + trader.getUserId(), pendingOrderDto);
    }

    private void validateSupportedCurrency(String currency) {
        try {
            Cryptocurrencies.Type.valueOf(currency);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
    }
    private void processOrderFunds(Trader trader, Order order) {
        if (isBuyOrder(order)) {
            processBuyOrderFunds(trader, order);
        } else if (isSellOrder(order)) {
            processSellOrderFunds(trader, order);
        }
    }
    private void processBuyOrderFunds(Trader trader, Order order) {
        BigDecimal cost = order.getPrice().multiply(order.getAmount());
        Wallet usdtWallet = walletService.findOrCreateWallet(trader, CURRENCY_USDT);

        validateSufficientFunds(usdtWallet.getAmount(), cost, "USDT");
        updateWalletAndNotify(trader, usdtWallet, usdtWallet.getAmount().subtract(cost), CURRENCY_USDT);
    }

    private void processSellOrderFunds(Trader trader, Order order) {
        Wallet currencyWallet = walletService.findOrCreateWallet(trader, order.getCurrency());

        validateSufficientFunds(currencyWallet.getAmount(), order.getAmount(), order.getCurrency());
        updateWalletAndNotify(trader, currencyWallet, currencyWallet.getAmount().subtract(order.getAmount()),
                order.getCurrency());
    }

    private void updateWalletAndNotify(Trader trader, Wallet wallet, BigDecimal newAmount, String currency) {
        wallet.setAmount(newAmount);
        walletRepository.save(wallet);

        WalletWebsocketDTO walletDTO = new WalletWebsocketDTO(wallet.getId(), trader.getId(), currency, newAmount);
        messagingTemplate.convertAndSend("/topic/wallets/" + trader.getUserId(), walletDTO);
    }
    private Trader getTraderById(Integer traderId) {
        return traderRepository.findById(traderId)
                .orElseThrow(() -> new IllegalArgumentException("Trader not found"));
    }
    private void validateSufficientFunds(BigDecimal available, BigDecimal required, String currency) {
        if (available.compareTo(required) < 0) {
            throw new IllegalArgumentException("Insufficient " + currency + " balance");
        }
    }
    private boolean isBuyOrder(Order order) {
        return BuyAndSell.Type.Buy.name().equalsIgnoreCase(order.getBuyAndSellType());
    }

    private boolean isSellOrder(Order order) {
        return BuyAndSell.Type.Sell.name().equalsIgnoreCase(order.getBuyAndSellType());
    }

    private boolean isLimitOrder(Order order) {
        return MarketAndLimit.LIMIT.getValue().equalsIgnoreCase(order.getMarketOrLimitOrderTypes());
    }
    private boolean isMarketOrder(Order order) {
        return MarketAndLimit.MARKET.getValue().equalsIgnoreCase(order.getMarketOrLimitOrderTypes());
    }
    private Order buildInitialOrder(Trader trader, OrderRequestDTO request) {
        return Order.builder()
                .trader(trader)
                .marketOrLimitOrderTypes(request.getMarketOrLimitOrderTypes())
                .price(request.getPrice())
                .amount(request.getAmount())
                .buyAndSellType(request.getBuyAndSellType())
                .currency(request.getCurrency())
                .orderStatus(MarketAndLimit.PENDING.getValue())
                .build();
    }


    /*

    replaced by getTraderById

        Trader trader = traderRepository.findById(requestDTO.getTraderId())
                .orElseThrow(() -> new IllegalArgumentException("Trader not found"));
        String userId = trader.getUserId();
//

replaced by buildInitialOrder

        Order order = Order.builder()
                .trader(trader)
                .marketOrLimitOrderTypes(requestDTO.getMarketOrLimitOrderTypes())
                .price(requestDTO.getPrice())
                .amount(requestDTO.getAmount())
                .buyAndSellType(requestDTO.getBuyAndSellType())
                .currency(requestDTO.getCurrency())
                .orderStatus("Pending")
                .build();

//

// already no need
private Trader getTraderByUserId(String userId) {
        return traderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found by id " + userId));
    }
    //


        if (String.valueOf(BuyAndSell.Type.Buy).equalsIgnoreCase(order.getBuyAndSellType())) {
            BigDecimal cost = order.getPrice().multiply(order.getAmount());
            Wallet usdtWallet = walletService.findOrCreateWallet(trader, CURRENCY_USDT);

            if (usdtWallet.getAmount().compareTo(cost) < 0) {
                throw new IllegalArgumentException("Insufficient USDT balance");
            }

            usdtWallet.setAmount(usdtWallet.getAmount().subtract(cost));
            walletRepository.save(usdtWallet);

            WalletWebsocketDTO usdtWalletDTO = new WalletWebsocketDTO(
                    usdtWallet.getId(),
                    trader.getId(),
                    CURRENCY_USDT,
                    usdtWallet.getAmount()
            );

            messagingTemplate.convertAndSend("/topic/wallets/" + userId, usdtWalletDTO);

        } else if (String.valueOf(BuyAndSell.Type.Sell).equalsIgnoreCase(order.getBuyAndSellType())) {
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
        if(MarketAndLimit.LIMIT.getValue().equalsIgnoreCase(order.getMarketOrLimitOrderTypes())) {
            String currency = order.getCurrency();

            if (!isSupportedCurrency(currency)) {
                throw new IllegalArgumentException("Unsupported currency: " + currency);
            }

            Double latestPrice = priceService.getLatestPrice(currency);

            if (latestPrice != null) {
                BigDecimal latestPriceBD = BigDecimal.valueOf(latestPrice);
                BigDecimal orderPrice = order.getPrice();

                if (!isOrderPriceWithinBounds(orderPrice, latestPriceBD)) {
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
        }*/

}