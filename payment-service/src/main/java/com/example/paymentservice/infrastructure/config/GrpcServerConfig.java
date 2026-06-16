package com.example.paymentservice.infrastructure.config;

import io.grpc.*;
import io.seata.core.context.RootContext;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    @GrpcGlobalServerInterceptor
    public ServerInterceptor globalServerInterceptor() {
        return new ServerInterceptor() {
            private final Metadata.Key<String> TX_XID_KEY = Metadata.Key.of("TX_XID", Metadata.ASCII_STRING_MARSHALLER);

            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                String xid = headers.get(TX_XID_KEY);
                boolean bind = false;
                if (xid != null && !xid.isEmpty()) {
                    RootContext.bind(xid);
                    bind = true;
                }
                try {
                    return next.startCall(call, headers);
                } finally {
                    if (bind) {
                        RootContext.unbind();
                    }
                }
            }
        };
    }
}
