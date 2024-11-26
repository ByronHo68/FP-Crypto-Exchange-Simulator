package com.Ron.tradingApps.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


@RestController
@RequestMapping("/appsuser")
public class AppsUserController {
    @GetMapping
    public String getAppsUserNameByPrincipal(Principal principal) {
        SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getName();
    }
}
