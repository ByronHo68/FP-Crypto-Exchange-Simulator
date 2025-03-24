package com.Ron.tradingApps.service.order;

import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.dto.response.TransactionResponseDTO;
import com.Ron.tradingApps.dto.response.WalletResponseDTO;
import com.Ron.tradingApps.mapper.OrderMapper;
import com.Ron.tradingApps.mapper.TransactionMapper;
import com.Ron.tradingApps.model.Order;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.model.Transaction;
import com.Ron.tradingApps.model.Wallet;
import com.Ron.tradingApps.model.type.BuyAndSell;
import com.Ron.tradingApps.repository.OrderRepository;
import com.Ron.tradingApps.repository.TraderRepository;
import com.Ron.tradingApps.repository.TransactionRepository;
import com.Ron.tradingApps.repository.WalletRepository;
import com.Ron.tradingApps.service.order.OrderService;
import com.Ron.tradingApps.service.wallet.WalletService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TransactionService {

    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final TraderRepository traderRepository;
    private final WalletRepository walletRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionMapper transactionMapper;

    public YourClassName(
            WalletService walletService,
            TransactionRepository transactionRepository,
            TraderRepository traderRepository,
            WalletRepository walletRepository,
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            OrderService orderService,
            SimpMessagingTemplate messagingTemplate,
            TransactionMapper transactionMapper
    ) {
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
        this.traderRepository = traderRepository;
        this.walletRepository = walletRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.messagingTemplate = messagingTemplate;
        this.transactionMapper = transactionMapper;
    }
    private static final String CURRENCY_USDT = "USDT";

    @Transactional
    public WalletResponseDTO createTransaction(OrderResponseDTO orderDTO) {
        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Trader trader = traderRepository.findById(orderDTO.getTraderId())
                .orElseThrow(() -> new IllegalArgumentException("Trader not found"));

        Wallet wallet = walletService.findByIdAndCurrencyOrCreate(orderDTO.getTraderId(), orderDTO.getCurrency());

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .order(order)
                .price(orderDTO.getPrice())
                .amount(orderDTO.getAmount())
                .trader(trader)
                .buyOrSellType(orderDTO.getBuyAndSellType())
                .currency(orderDTO.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        order.setOrderStatus("Complete");
        orderRepository.save(order);

        messagingTemplate.convertAndSend("/topic/orders/pending", order.getId());

        return updateWalletsBasedOnTransaction(transaction, trader);
    }

    @Transactional
    private WalletResponseDTO updateWalletsBasedOnTransaction(Transaction transaction, Trader trader) {
        BigDecimal amount = transaction.getAmount();
        BigDecimal price = transaction.getPrice();


        if (String.valueOf(BuyAndSell.Type.Buy).equalsIgnoreCase(transaction.getBuyOrSellType())) {
            Wallet usdtWallet = walletService.findOrCreateWallet(trader, CURRENCY_USDT);


            Wallet currencyWallet = walletService.findOrCreateWallet(trader, transaction.getCurrency());

            currencyWallet.setAmount(currencyWallet.getAmount().add(amount));
            walletRepository.save(currencyWallet);
            String userId = trader.getUserId();
            messagingTemplate.convertAndSend("/topic/wallets/" + userId, currencyWallet);

            return createWalletResponse(trader, usdtWallet, currencyWallet);

        } else if (String.valueOf(BuyAndSell.Type.Sell).equalsIgnoreCase(transaction.getBuyOrSellType())) {

            Wallet currencyWallet = walletService.findByIdAndCurrencyOrCreate(trader.getId(), transaction.getCurrency());
            if (currencyWallet == null || currencyWallet.getAmount().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient " + transaction.getCurrency() + " balance in trader's wallet");
            }

            Wallet usdtWallet = walletService.findOrCreateWallet(trader, CURRENCY_USDT);

            BigDecimal totalRevenue = amount.multiply(price);

            usdtWallet.setAmount(usdtWallet.getAmount().add(totalRevenue));
            walletRepository.save(usdtWallet);
            String userId = trader.getUserId();
            messagingTemplate.convertAndSend("/topic/wallets/" + userId, usdtWallet);

            return createWalletResponse(trader, usdtWallet, currencyWallet);

        } else {
            throw new IllegalArgumentException("Invalid buy/sell type: " + transaction.getBuyOrSellType());
        }
    }

    private WalletResponseDTO createWalletResponse(Trader trader, Wallet usdtWallet, Wallet currencyWallet) {
        WalletResponseDTO response = new WalletResponseDTO();
        response.setUSDTWalletId(usdtWallet.getId());
        response.setCurrencyWalletId(currencyWallet.getId());
        response.setTraderId(trader.getId());
        response.setCurrency(currencyWallet.getCurrency());
        response.setUSDTAmount(usdtWallet.getAmount());
        response.setCurrencyAmount(currencyWallet.getAmount());
        return response;
    }
    public List<TransactionResponseDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }
}