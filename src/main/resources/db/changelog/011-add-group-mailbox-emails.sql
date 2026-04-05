-- Почтовые ящики очередей ARM; id групп совпадают с mailbox id в email_service (домен system-alerts.ru).

ALTER TABLE arm_support.assignment_groups
    ADD COLUMN mailbox_email VARCHAR(320);

ALTER TABLE arm_support.skill_groups
    ADD COLUMN mailbox_email VARCHAR(320);

CREATE UNIQUE INDEX idx_assignment_groups_mailbox_email
    ON arm_support.assignment_groups (mailbox_email)
    WHERE mailbox_email IS NOT NULL;

CREATE UNIQUE INDEX idx_skill_groups_mailbox_email
    ON arm_support.skill_groups (mailbox_email)
    WHERE mailbox_email IS NOT NULL;

UPDATE arm_support.assignment_groups
SET mailbox_email = 'contact-center-first-line@system-alerts.ru'
WHERE id = 'a1b2c3d4-1001-4000-8000-000000000001'::uuid;

UPDATE arm_support.assignment_groups
SET mailbox_email = 'qualification-routing@system-alerts.ru'
WHERE id = 'a1b2c3d4-1001-4000-8000-000000000002'::uuid;

UPDATE arm_support.assignment_groups
SET mailbox_email = 'digital-banking@system-alerts.ru'
WHERE id = 'a1b2c3d4-1001-4000-8000-000000000003'::uuid;

UPDATE arm_support.assignment_groups
SET mailbox_email = 'cards-and-payments@system-alerts.ru'
WHERE id = 'a1b2c3d4-1001-4000-8000-000000000004'::uuid;

UPDATE arm_support.assignment_groups
SET mailbox_email = 'loans-deposits@system-alerts.ru'
WHERE id = 'a1b2c3d4-1001-4000-8000-000000000005'::uuid;

UPDATE arm_support.assignment_groups
SET mailbox_email = 'second-line-systems@system-alerts.ru'
WHERE id = 'a1b2c3d4-1001-4000-8000-000000000006'::uuid;

UPDATE arm_support.assignment_groups
SET mailbox_email = 'premium-clients@system-alerts.ru'
WHERE id = 'a1b2c3d4-1001-4000-8000-000000000007'::uuid;

UPDATE arm_support.assignment_groups
SET mailbox_email = 'corporate-sme@system-alerts.ru'
WHERE id = 'a1b2c3d4-1001-4000-8000-000000000008'::uuid;

UPDATE arm_support.skill_groups
SET mailbox_email = 'skill-client-communication@system-alerts.ru'
WHERE id = 'b1b2c3d4-2001-4000-8000-000000000001'::uuid;

UPDATE arm_support.skill_groups
SET mailbox_email = 'skill-digital-banking@system-alerts.ru'
WHERE id = 'b1b2c3d4-2001-4000-8000-000000000002'::uuid;

UPDATE arm_support.skill_groups
SET mailbox_email = 'skill-card-payments@system-alerts.ru'
WHERE id = 'b1b2c3d4-2001-4000-8000-000000000003'::uuid;

UPDATE arm_support.skill_groups
SET mailbox_email = 'skill-lending-products@system-alerts.ru'
WHERE id = 'b1b2c3d4-2001-4000-8000-000000000004'::uuid;

UPDATE arm_support.skill_groups
SET mailbox_email = 'skill-incident-management@system-alerts.ru'
WHERE id = 'b1b2c3d4-2001-4000-8000-000000000005'::uuid;

UPDATE arm_support.skill_groups
SET mailbox_email = 'skill-fraud-compliance@system-alerts.ru'
WHERE id = 'b1b2c3d4-2001-4000-8000-000000000006'::uuid;

UPDATE arm_support.skill_groups
SET mailbox_email = 'skill-corporate-banking@system-alerts.ru'
WHERE id = 'b1b2c3d4-2001-4000-8000-000000000007'::uuid;
