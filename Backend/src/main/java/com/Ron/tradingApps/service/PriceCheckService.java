package com.Ron.tradingApps.service;

import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import com.Ron.tradingApps.service.data.*;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PriceCheckService {
    @Autowired
    private PriceService priceService;
    @Autowired
    private TransactionService transactionService;

    public void checkAndExecuteTransaction(OrderResponseDTO orderResponse) {
        String currency = orderResponse.getCurrency();

        if (!currency.equals("ETHUSDT") && !currency.equals("BTCUSDT")) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }

        Double latestPrice = priceService.getLatestPrice(currency);

        if (latestPrice != null) {
            BigDecimal latestPriceBD = BigDecimal.valueOf(latestPrice);
            BigDecimal orderPrice = orderResponse.getPrice();

            BigDecimal onePercentDifference = latestPriceBD.multiply(BigDecimal.valueOf(0.01));
            BigDecimal lowerBound = latestPriceBD.subtract(onePercentDifference);
            BigDecimal upperBound = latestPriceBD.add(onePercentDifference);

            if(orderResponse.getBuyAndSellType().equalsIgnoreCase("sell") && orderPrice.compareTo(lowerBound) >= 0 && orderPrice.compareTo(upperBound) <= 0){
                transactionService.createTransaction(orderResponse);
                //set price limit for here if needed
            }

            if (orderPrice.compareTo(lowerBound) >= 0 && orderPrice.compareTo(upperBound) <= 0) {
                transactionService.createTransaction(orderResponse);
            } else {
                /*System.out.println("Latest price for " + currency + " is outside of 1% range: " + orderPrice + " upper: " + upperBound + " lower: " + lowerBound);*/
            }
        } else {
            System.out.println("Latest price not found for " + currency);
        }
    }
}