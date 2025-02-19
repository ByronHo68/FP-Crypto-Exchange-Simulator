package com.Ron.tradingApps.controller.admin;

import com.Ron.tradingApps.dto.WalletDTO;
import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.dto.response.TraderResponseDTO;
import com.Ron.tradingApps.repository.OrderRepository;
import com.Ron.tradingApps.repository.TraderRepository;
import com.Ron.tradingApps.repository.TransactionRepository;
import com.Ron.tradingApps.repository.WalletRepository;
import com.Ron.tradingApps.service.order.OrderService;
import com.Ron.tradingApps.service.user.TraderService;
import com.Ron.tradingApps.service.order.TransactionService;
import com.Ron.tradingApps.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminTestController {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TraderRepository traderRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TraderService traderService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private OrderService orderService;



    @GetMapping("/user-info")
    public Map<String, String> getAdminInfo(Principal principal){
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;

        System.out.println(token.getTokenAttributes());
        return Map.of(
                "displayName",token.getTokenAttributes().get("name").toString(),
                "email",token.getTokenAttributes().get("email").toString(),
                "userId", token.getTokenAttributes().get("userId").toString(),
                "role",token.getTokenAttributes().get("custom_claims").toString()
        );
    }
    @GetMapping("/orders")
    public List<OrderResponseDTO> getAllOrders(Principal principal){
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        System.out.println(token.getTokenAttributes());
        return orderService.getAllTransactions();
    }

    @GetMapping("/wallets")
    public List<WalletDTO> getAllWallets(Principal principal){
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        System.out.println(token.getTokenAttributes());
        return walletService.getAllWallets();
    }
    @GetMapping("/traders")
    public List<TraderResponseDTO> getAllTraders(Principal principal){
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        System.out.println(token.getTokenAttributes());
        return traderService.getAllTraders();
    }
}
