-- Reference schema for a persistent PostgreSQL deployment.
-- Tests use Hibernate-generated H2 tables; this script is not run by the tests.
CREATE TABLE purchase_order (
    id UUID PRIMARY KEY,
    status VARCHAR(255) NOT NULL
);

CREATE TABLE audit_entry (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    action VARCHAR(255) NOT NULL
);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);
