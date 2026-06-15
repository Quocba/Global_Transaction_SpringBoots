CREATE DATABASE "OrderService";
\c "OrderService";
CREATE TABLE IF NOT EXISTS undo_log
(
    id            SERIAL PRIMARY KEY,
    branch_id     BIGINT       NOT NULL,
    xid           VARCHAR(128) NOT NULL,
    context       VARCHAR(128) NOT NULL,
    rollback_info BYTEA        NOT NULL,
    log_status    INT          NOT NULL,
    log_created   TIMESTAMP(0) NOT NULL,
    log_modified  TIMESTAMP(0) NOT NULL,
    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
);
CREATE INDEX IF NOT EXISTS ix_undo_log_xid_branch_id ON undo_log(xid, branch_id);

CREATE DATABASE "PaymentService";
\c "PaymentService";
CREATE TABLE IF NOT EXISTS undo_log
(
    id            SERIAL PRIMARY KEY,
    branch_id     BIGINT       NOT NULL,
    xid           VARCHAR(128) NOT NULL,
    context       VARCHAR(128) NOT NULL,
    rollback_info BYTEA        NOT NULL,
    log_status    INT          NOT NULL,
    log_created   TIMESTAMP(0) NOT NULL,
    log_modified  TIMESTAMP(0) NOT NULL,
    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
);
CREATE INDEX IF NOT EXISTS ix_undo_log_xid_branch_id ON undo_log(xid, branch_id);

