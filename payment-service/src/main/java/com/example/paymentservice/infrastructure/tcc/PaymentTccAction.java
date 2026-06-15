package com.example.paymentservice.infrastructure.tcc;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import java.math.BigDecimal;

@LocalTCC
public interface PaymentTccAction {
    @TwoPhaseBusinessAction(name = "paymentTccAction", commitMethod = "commit", rollbackMethod = "rollback")
    boolean prepare(BusinessActionContext context,
                    @BusinessActionContextParameter("orderId") Long orderId,
                    @BusinessActionContextParameter("amount") BigDecimal amount,
                    @BusinessActionContextParameter("simulatePaymentError") Boolean simulatePaymentError);

    boolean commit(BusinessActionContext context);

    boolean rollback(BusinessActionContext context);
}
