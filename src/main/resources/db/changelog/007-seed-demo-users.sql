-- liquibase formatted sql
-- changeset arm-support:007-seed-demo-users runOnChange:false

INSERT INTO arm_support.synced_users (id, email, username, first_name, last_name, status, email_verified, roles, synced_at,
                                        created_at)
VALUES ('d0e0f000-d001-4000-8000-000000000001'::uuid,
        'demo.admin1@demo.local',
        'demo_admin_1',
        'Демо',
        'Админ 1',
        'ACTIVE',
        TRUE,
        ARRAY ['ROLE_USER', 'ROLE_ADMIN']::TEXT[],
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('d0e0f000-d001-4000-8000-000000000002'::uuid,
        'demo.admin2@demo.local',
        'demo_admin_2',
        'Демо',
        'Админ 2',
        'ACTIVE',
        TRUE,
        ARRAY ['ROLE_USER', 'ROLE_ADMIN']::TEXT[],
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO arm_support.synced_users (id, email, username, first_name, last_name, status, email_verified, roles, synced_at,
                                        created_at)
VALUES ('d0e0f000-d001-4000-8000-000000000003'::uuid,
        'demo.operator1@demo.local',
        'demo_operator_1',
        'Демо',
        'Оператор 1',
        'ACTIVE',
        TRUE,
        ARRAY ['ROLE_USER', 'ROLE_OPERATOR']::TEXT[],
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('d0e0f000-d001-4000-8000-000000000004'::uuid,
        'demo.operator2@demo.local',
        'demo_operator_2',
        'Демо',
        'Оператор 2',
        'ACTIVE',
        TRUE,
        ARRAY ['ROLE_USER', 'ROLE_OPERATOR']::TEXT[],
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO arm_support.assignment_group_operators (group_id, operator_id)
VALUES ('a1b2c3d4-1001-4000-8000-000000000001'::uuid, 'd0e0f000-d001-4000-8000-000000000003'::uuid),
       ('a1b2c3d4-1001-4000-8000-000000000002'::uuid, 'd0e0f000-d001-4000-8000-000000000003'::uuid),
       ('a1b2c3d4-1001-4000-8000-000000000003'::uuid, 'd0e0f000-d001-4000-8000-000000000003'::uuid),
       ('a1b2c3d4-1001-4000-8000-000000000004'::uuid, 'd0e0f000-d001-4000-8000-000000000003'::uuid);

INSERT INTO arm_support.skill_group_operators (group_id, operator_id)
VALUES ('b1b2c3d4-2001-4000-8000-000000000001'::uuid, 'd0e0f000-d001-4000-8000-000000000003'::uuid),
       ('b1b2c3d4-2001-4000-8000-000000000002'::uuid, 'd0e0f000-d001-4000-8000-000000000003'::uuid),
       ('b1b2c3d4-2001-4000-8000-000000000003'::uuid, 'd0e0f000-d001-4000-8000-000000000003'::uuid),
       ('b1b2c3d4-2001-4000-8000-000000000004'::uuid, 'd0e0f000-d001-4000-8000-000000000003'::uuid);

INSERT INTO arm_support.assignment_group_operators (group_id, operator_id)
VALUES ('a1b2c3d4-1001-4000-8000-000000000005'::uuid, 'd0e0f000-d001-4000-8000-000000000004'::uuid),
       ('a1b2c3d4-1001-4000-8000-000000000006'::uuid, 'd0e0f000-d001-4000-8000-000000000004'::uuid),
       ('a1b2c3d4-1001-4000-8000-000000000007'::uuid, 'd0e0f000-d001-4000-8000-000000000004'::uuid),
       ('a1b2c3d4-1001-4000-8000-000000000008'::uuid, 'd0e0f000-d001-4000-8000-000000000004'::uuid);

INSERT INTO arm_support.skill_group_operators (group_id, operator_id)
VALUES ('b1b2c3d4-2001-4000-8000-000000000005'::uuid, 'd0e0f000-d001-4000-8000-000000000004'::uuid),
       ('b1b2c3d4-2001-4000-8000-000000000006'::uuid, 'd0e0f000-d001-4000-8000-000000000004'::uuid),
       ('b1b2c3d4-2001-4000-8000-000000000007'::uuid, 'd0e0f000-d001-4000-8000-000000000004'::uuid);
