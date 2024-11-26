package com.Ron.tradingApps.firebase;

import com.Ron.tradingApps.dto.request.SignUpRequestDTO;
import com.Ron.tradingApps.mapper.TraderMapper;
import com.Ron.tradingApps.model.Role;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FirebaseAuthService {
    @Autowired
    private FirebaseAuth firebaseAuth;

    @Autowired
    private TraderMapper traderMapper;


    public String getFirebaseUID(SignUpRequestDTO requestDTO) throws FirebaseAuthException {

        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setDisplayName(requestDTO.getDisplayName())
                .setEmail(requestDTO.getEmail())
                .setPassword(requestDTO.getPassword());

        UserRecord userRecord = firebaseAuth.createUser(createRequest);
        String uid = userRecord.getUid();


        log.info("[FirebaseAuthService] Created user [{}] for [{}]", uid, userRecord.getEmail());
        return uid;
    }

    public void setUserClaims(String uid, List<Role> requestedPermissions) throws FirebaseAuthException {
        List<String> permissions = requestedPermissions.stream().map(Enum::toString).toList();

        Map<String, Object> claims = Map.of("custom_claims", permissions);

        firebaseAuth.setCustomUserClaims(uid, claims);
    }
}