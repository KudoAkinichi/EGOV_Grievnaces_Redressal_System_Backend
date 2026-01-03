package com.grievance.auth.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class InternalServiceAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final String internalToken;

    public InternalServiceAuthorizationManager(
            @org.springframework.beans.factory.annotation.Value("${internal.service.token}")
            String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    public AuthorizationDecision check(
            java.util.function.Supplier<org.springframework.security.core.Authentication> authentication,
            RequestAuthorizationContext context) {

        HttpServletRequest request = context.getRequest();
        String token = request.getHeader("X-INTERNAL-TOKEN");

        boolean allowed = internalToken.equals(token);
        return new AuthorizationDecision(allowed);
    }
}
