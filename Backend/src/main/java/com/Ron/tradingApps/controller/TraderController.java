package com.Ron.tradingApps.controller;

import com.Ron.tradingApps.dto.request.TraderRequestDTO;
import com.Ron.tradingApps.dto.response.TraderDashboardResponseDTO;
import com.Ron.tradingApps.dto.request.TransferRequestDTO;
import com.Ron.tradingApps.mapper.TraderMapper;
import com.Ron.tradingApps.dto.response.TraderResponseDTO;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.repository.TraderRepository;
import com.Ron.tradingApps.service.TraderService;
import com.Ron.tradingApps.service.WalletService;
import jakarta.validation.Valid;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/traders")
public class TraderController {
    @Autowired
    private TraderMapper traderMapper;
    @Autowired
    private TraderService traderService;
    @Autowired
    private WalletService walletService;

    @GetMapping("/{username}")
    public TraderResponseDTO findTraderByUsername(@PathVariable("username") String username) throws ResourceNotFoundException{
        return traderMapper.toDTO(traderService.findByUsername(username));
    }

    @PostMapping
    public TraderResponseDTO createTrader(@Valid @RequestBody TraderRequestDTO requestDTO){
        Trader trader = traderService.save(traderMapper.toEntity(requestDTO));
        return traderMapper.toDTO(trader);
    }
    @PutMapping("/{username}")
    public TraderResponseDTO updateTrader(@PathVariable("username") String username,
                                          @Valid @RequestBody TraderRequestDTO requestDTO) throws ResourceNotFoundException {
        Trader updateTrader = traderService.update(username, traderMapper.toEntity(requestDTO), requestDTO.getPassword());
        return traderMapper.toDTO(updateTrader);
    }
    @GetMapping("/id/{userId}")
    public ResponseEntity<TraderDashboardResponseDTO> getTrader(@PathVariable String userId) {
        return traderService.getTraderByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/transfer-usdt")
    public ResponseEntity<String> transferUsdtToWallet(@Valid @RequestBody TransferRequestDTO transferRequest) {
        try {
            Trader trader = traderService.findById(transferRequest.getTraderId());
            walletService.transferUsdtToWallet(trader, transferRequest.getAmount());
            return ResponseEntity.ok("Transfer successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
