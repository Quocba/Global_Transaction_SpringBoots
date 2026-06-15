package com.example.orderservice.infrastructure.tcc;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import java.math.BigDecimal;

@LocalTCC
public interface OrderTccAction {
    @TwoPhaseBusinessAction(name = "orderTccAction", commitMethod = "commit", rollbackMethod = "rollback")
    Long prepare(BusinessActionContext context,
                 @BusinessActionContextParameter("productId") String productId,
                 @BusinessActionContextParameter("quantity") Integer quantity,
                 @BusinessActionContextParameter("price") BigDecimal price);

    boolean commit(BusinessActionContext context);

    boolean rollback(BusinessActionContext context);
}
