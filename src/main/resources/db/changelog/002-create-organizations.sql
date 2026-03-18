CREATE TABLE arm_support.organizations
(
    id            UUID         NOT NULL PRIMARY KEY,
    name          VARCHAR(512) NOT NULL,
    inn           VARCHAR(12)  NOT NULL,
    kpp           VARCHAR(9),
    ogrn          VARCHAR(15),
    legal_address TEXT,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(32),
    description   TEXT,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_organizations_inn ON arm_support.organizations (inn);
CREATE INDEX idx_organizations_name ON arm_support.organizations (name);
