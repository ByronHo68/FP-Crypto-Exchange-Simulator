package com.Ron.tradingApps.service;

import com.Ron.tradingApps.dto.TraderDTO;
import com.Ron.tradingApps.dto.request.TraderRequestDTO;
import com.Ron.tradingApps.dto.response.TraderDashboardResponseDTO;
import com.Ron.tradingApps.dto.response.TraderResponseDTO;
import com.Ron.tradingApps.mapper.TraderMapper;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.repository.TraderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TraderService {
    @Autowired
    private TraderRepository traderRepository;
    @Autowired
    private TraderMapper traderMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Trader findByUsername(String username) throws ResourceNotFoundException{
        return traderRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found by id " + username
                ));
    }


    @Transactional
    public Trader save(Trader trader) {
        return traderRepository.save(trader);
    }

    @Transactional
    public Trader update(String username, Trader trader, String providedPassword) throws ResourceNotFoundException {
        Trader existingTrader = findByUsername(username);

        if (!passwordEncoder.matches(providedPassword, existingTrader.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        existingTrader.setEmail(trader.getEmail());
        existingTrader.setFirstName(trader.getFirstName());
        existingTrader.setLastName(trader.getLastName());
        existingTrader.setCreatedAt(LocalDateTime.now());
        existingTrader.setUpdatedAt(LocalDateTime.now());
        existingTrader.setUserId(trader.getUserId());
        existingTrader.setUsdtBalance(trader.getUsdtBalance());
        existingTrader.setIdNumber(trader.getIdNumber());
        existingTrader.setPhoneNumber(trader.getPhoneNumber());
        existingTrader.setYesterdayPrice(trader.getYesterdayPrice() != null ? trader.getYesterdayPrice() : BigDecimal.ZERO);

        return traderRepository.save(existingTrader);
    }

    @Transactional
    public void deleteByUsername(String username) throws ResourceNotFoundException {
        Trader existingTrader = findByUsername(username);
        traderRepository.delete(existingTrader);
    }

    @Transactional
    public Trader createTrader(TraderRequestDTO requestDTO) {
        if (requestDTO.getUsername() == null || requestDTO.getEmail() == null || requestDTO.getPassword() == null) {
            throw new IllegalArgumentException("Username, email, and password must not be null");
        }
        String encryptedPassword = passwordEncoder.encode(requestDTO.getPassword());
        Trader trader = Trader.builder()
                .username(requestDTO.getUsername())
                .email(requestDTO.getEmail())
                .password(encryptedPassword)
                .userId(requestDTO.getUserId())
                .firstName("haven't finished KYC")
                .lastName("haven't finished KYC")
                .yesterdayPrice(BigDecimal.ZERO)
                .phoneNumber("haven't finished KYC")
                .updatedAt(LocalDateTime.now())
                .usdtBalance(BigDecimal.ZERO)
                .idNumber("haven't finished KYC")
                .build();

        return traderRepository.save(trader);
    }


    public Optional<TraderDashboardResponseDTO> getTraderByUserId(String userId) {
        Optional<Trader> traderOpt = traderRepository.findByUserId(userId);
        return traderOpt.map(trader -> new TraderDashboardResponseDTO(
                trader.getId(),
                trader.getUsername(),
                trader.getEmail(),
                trader.getFirstName(),
                trader.getLastName(),
                trader.getUserId(),
                trader.getPhoneNumber(),
                trader.getUsdtBalance(),
                trader.getYesterdayPrice()
        ));
    }

    public Trader findById(Integer traderId) {
        return traderRepository.findById(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found with id: " + traderId));
    }

    public List<TraderResponseDTO> getAllTraders() {
        List<Trader> traders = traderRepository.findAll();
        return traders.stream()
                .map(traderMapper::toDTO)
                .collect(Collectors.toList());
    }
}
