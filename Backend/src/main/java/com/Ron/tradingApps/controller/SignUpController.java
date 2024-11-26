package com.Ron.tradingApps.controller;

import com.Ron.tradingApps.dto.request.SignUpRequestDTO;
import com.Ron.tradingApps.dto.request.TraderRequestDTO;
import com.Ron.tradingApps.dto.response.SignUpResponseDTO;
import com.Ron.tradingApps.exception.UnauthorizedOperationException;
import com.Ron.tradingApps.firebase.FirebaseAuthService;
import com.Ron.tradingApps.model.Role;
import com.Ron.tradingApps.service.InstructorService;
import com.Ron.tradingApps.service.TraderService;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/signup")
public class SignUpController {
    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private TraderService traderService;
    @Autowired
    private InstructorService instructorService;

    @Value("${admin.secret.code}")
    private String adminSecretCode;

    @PostMapping("/user")
    public SignUpResponseDTO signupUser(@Valid @RequestBody SignUpRequestDTO requestDTO) throws FirebaseAuthException {
        String firebaseUID = firebaseAuthService.getFirebaseUID(requestDTO);
        firebaseAuthService.setUserClaims(firebaseUID, List.of(Role.USER));


        createTrader(firebaseUID, requestDTO);

        return SignUpResponseDTO.builder()
                .firebaseUID(firebaseUID)
                .build();
    }

    private void createTrader(String uid, SignUpRequestDTO requestDTO) {
        TraderRequestDTO traderRequestDTO = new TraderRequestDTO();
        traderRequestDTO.setUsername(requestDTO.getDisplayName());
        traderRequestDTO.setEmail(requestDTO.getEmail());
        traderRequestDTO.setPassword(requestDTO.getPassword());
        traderRequestDTO.setUserId(uid);
        traderRequestDTO.setUsdtBalance(BigDecimal.ZERO);
        traderRequestDTO.setIdNumber("0");
        traderRequestDTO.setPhoneNumber("0");
        traderRequestDTO.setYesterdayPrice(BigDecimal.ZERO);

        LocalDateTime now = LocalDateTime.now();
        traderRequestDTO.setCreatedAt(now);
        traderRequestDTO.setUpdatedAt(now);


        traderService.createTrader(traderRequestDTO);
    }

    @PostMapping("/admin")
    public SignUpResponseDTO signupAdmin(@Valid @RequestBody SignUpRequestDTO requestDTO)
            throws FirebaseAuthException, UnauthorizedOperationException {

        if (!adminSecretCode.equals(requestDTO.getSecretCode())) {
            throw new UnauthorizedOperationException("Invalid admin secret code");
        }

        String firebaseUID = firebaseAuthService.getFirebaseUID(requestDTO);
        System.out.println(firebaseUID);
        firebaseAuthService.setUserClaims(firebaseUID, List.of(Role.USER, Role.ADMIN));


        FirebaseAuth.getInstance().setCustomUserClaims(firebaseUID, Map.of("custom_claims", List.of("USER", "ADMIN")));


        instructorService.createInstructor(firebaseUID, requestDTO);

        return SignUpResponseDTO.builder()
                .firebaseUID(firebaseUID)
                .build();
    }
}