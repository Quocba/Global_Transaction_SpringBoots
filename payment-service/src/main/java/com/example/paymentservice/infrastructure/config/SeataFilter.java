package com.example.paymentservice.infrastructure.config;

import io.seata.core.context.RootContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class SeataFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String xid = req.getHeader("TX_XID");
        boolean bind = false;
        if (xid != null && !xid.isEmpty()) {
            RootContext.bind(xid);
            bind = true;
        }
        try {
            chain.doFilter(request, response);
        } finally {
            if (bind) {
                RootContext.unbind();
            }
        }
    }
}
