package com.example.coordinatorservice.infrastructure.config;

import io.grpc.*;
import io.seata.core.context.RootContext;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean
    @GrpcGlobalClientInterceptor
    public ClientInterceptor globalClientInterceptor() {
        return new ClientInterceptor() {
            private final Metadata.Key<String> TX_XID_KEY =
                    Metadata.Key.of("TX_XID", Metadata.ASCII_STRING_MARSHALLER);

            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
                return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        String xid = RootContext.getXID();
                        if (xid != null && !xid.isEmpty()) {
                            headers.put(TX_XID_KEY, xid);
                        }
                        super.start(responseListener, headers);
                    }
                };
            }
        };
    }
}
