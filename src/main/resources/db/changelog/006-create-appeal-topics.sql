CREATE TABLE arm_support.appeal_topics
(
    id          UUID         NOT NULL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    category    VARCHAR(64)  NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_appeal_topics_code ON arm_support.appeal_topics (code);
CREATE INDEX idx_appeal_topics_category ON arm_support.appeal_topics (category);
CREATE INDEX idx_appeal_topics_active ON arm_support.appeal_topics (active);

-- =============================================================================
-- Начальный справочник тематик для операторов техподдержки банка
-- =============================================================================

-- ACCOUNT_AND_CARD
INSERT INTO arm_support.appeal_topics (id, code, name, category, description, active, created_at) VALUES
('00000000-0000-0000-0001-000000000001', 'CARD_BLOCK',            'Блокировка / разблокировка карты',       'ACCOUNT_AND_CARD',        'Запрос на блокировку или разблокировку платёжной карты',               TRUE, NOW()),
('00000000-0000-0000-0001-000000000002', 'CARD_REISSUE',          'Перевыпуск карты',                       'ACCOUNT_AND_CARD',        'Плановый или внеплановый перевыпуск карты',                            TRUE, NOW()),
('00000000-0000-0000-0001-000000000003', 'CARD_UNAUTHORIZED_OPS', 'Несанкционированные операции по карте',  'ACCOUNT_AND_CARD',        'Оспаривание операций, не совершённых клиентом',                       TRUE, NOW()),
('00000000-0000-0000-0001-000000000004', 'ACCOUNT_BALANCE',       'Проблема с балансом счёта',              'ACCOUNT_AND_CARD',        'Несоответствие баланса, задержка отражения операций',                  TRUE, NOW()),
('00000000-0000-0000-0001-000000000005', 'ACCOUNT_OPEN_CLOSE',    'Открытие / закрытие счёта',              'ACCOUNT_AND_CARD',        'Запросы на открытие нового счёта или закрытие существующего',          TRUE, NOW());

-- DIGITAL_BANKING
INSERT INTO arm_support.appeal_topics (id, code, name, category, description, active, created_at) VALUES
('00000000-0000-0000-0002-000000000001', 'IB_LOGIN',              'Проблема со входом в интернет-банк',     'DIGITAL_BANKING',         'Не удаётся авторизоваться в ЛК / интернет-банке',                     TRUE, NOW()),
('00000000-0000-0000-0002-000000000002', 'MOBILE_APP',            'Проблема с мобильным приложением',       'DIGITAL_BANKING',         'Ошибки, сбои или некорректная работа мобильного банка',               TRUE, NOW()),
('00000000-0000-0000-0002-000000000003', 'IB_OPERATION_ERROR',    'Ошибка при выполнении операции в ЛК',   'DIGITAL_BANKING',         'Операция не проходит или завершается с ошибкой в личном кабинете',    TRUE, NOW()),
('00000000-0000-0000-0002-000000000004', 'NOTIFICATIONS_SETUP',   'Настройка уведомлений',                  'DIGITAL_BANKING',         'Подключение, отключение или настройка SMS/push-уведомлений',          TRUE, NOW());

-- PAYMENTS_AND_TRANSFERS
INSERT INTO arm_support.appeal_topics (id, code, name, category, description, active, created_at) VALUES
('00000000-0000-0000-0003-000000000001', 'PAYMENT_DELAY',         'Задержка зачисления платежа',            'PAYMENTS_AND_TRANSFERS',  'Платёж отправлен, но не зачислен получателю в ожидаемые сроки',      TRUE, NOW()),
('00000000-0000-0000-0003-000000000002', 'WRONG_TRANSFER',        'Ошибочный перевод',                      'PAYMENTS_AND_TRANSFERS',  'Перевод выполнен на неверные реквизиты или в неверной сумме',         TRUE, NOW()),
('00000000-0000-0000-0003-000000000003', 'OPERATION_CANCEL',      'Отмена / возврат операции',              'PAYMENTS_AND_TRANSFERS',  'Запрос на отмену проведённой операции или возврат средств',           TRUE, NOW()),
('00000000-0000-0000-0003-000000000004', 'PAYMENT_ORDER',         'Проблема с платёжным поручением',        'PAYMENTS_AND_TRANSFERS',  'Ошибки в оформлении или исполнении платёжного поручения',             TRUE, NOW());

-- LOANS_AND_CREDITS
INSERT INTO arm_support.appeal_topics (id, code, name, category, description, active, created_at) VALUES
('00000000-0000-0000-0004-000000000001', 'LOAN_CONDITIONS',       'Вопрос по условиям кредита',             'LOANS_AND_CREDITS',       'Уточнение ставки, графика погашения, условий договора',               TRUE, NOW()),
('00000000-0000-0000-0004-000000000002', 'LOAN_RESTRUCTURING',    'Реструктуризация задолженности',         'LOANS_AND_CREDITS',       'Запрос на изменение условий погашения кредита',                       TRUE, NOW()),
('00000000-0000-0000-0004-000000000003', 'EARLY_REPAYMENT',       'Досрочное погашение',                    'LOANS_AND_CREDITS',       'Полное или частичное досрочное погашение кредита',                    TRUE, NOW()),
('00000000-0000-0000-0004-000000000004', 'CREDIT_HISTORY',        'Проблема с кредитной историей',          'LOANS_AND_CREDITS',       'Ошибки или спорные данные в кредитной истории',                      TRUE, NOW());

-- SECURITY
INSERT INTO arm_support.appeal_topics (id, code, name, category, description, active, created_at) VALUES
('00000000-0000-0000-0005-000000000001', 'FRAUD_SUSPICION',       'Подозрение на мошенничество',            'SECURITY',                'Клиент подозревает мошеннические действия с его данными или счётом',  TRUE, NOW()),
('00000000-0000-0000-0005-000000000002', 'CARD_COMPROMISE',       'Компрометация данных карты',             'SECURITY',                'Данные карты могли стать известны третьим лицам',                    TRUE, NOW()),
('00000000-0000-0000-0005-000000000003', 'CHANGE_CREDENTIALS',    'Смена пароля / PIN-кода',                'SECURITY',                'Запрос на изменение пароля ЛК или PIN-кода карты',                    TRUE, NOW()),
('00000000-0000-0000-0005-000000000004', 'RESTRICT_OPERATIONS',   'Ограничение операций',                   'SECURITY',                'Запрос на временное ограничение операций по счёту / карте',           TRUE, NOW());

-- TECHNICAL_ISSUES
INSERT INTO arm_support.appeal_topics (id, code, name, category, description, active, created_at) VALUES
('00000000-0000-0000-0006-000000000001', 'SERVICE_UNAVAILABLE',   'Сервис временно недоступен',             'TECHNICAL_ISSUES',        'Банковский сервис или портал не отвечает',                            TRUE, NOW()),
('00000000-0000-0000-0006-000000000002', 'APP_ERROR',             'Ошибка в работе приложения',             'TECHNICAL_ISSUES',        'Технический сбой в интернет-банке или мобильном приложении',          TRUE, NOW()),
('00000000-0000-0000-0006-000000000003', 'INTEGRATION_ISSUE',     'Проблема с интеграцией',                 'TECHNICAL_ISSUES',        'Сбой во взаимодействии с внешними системами (1С, ERP и др.)',         TRUE, NOW());

-- GENERAL
INSERT INTO arm_support.appeal_topics (id, code, name, category, description, active, created_at) VALUES
('00000000-0000-0000-0007-000000000001', 'PRODUCT_CONSULT',       'Консультация по продуктам банка',        'GENERAL',                 'Вопросы по условиям, тарифам и продуктам банка',                      TRUE, NOW()),
('00000000-0000-0000-0007-000000000002', 'SERVICE_COMPLAINT',     'Жалоба на качество обслуживания',        'GENERAL',                 'Недовольство уровнем или качеством клиентского сервиса',              TRUE, NOW()),
('00000000-0000-0000-0007-000000000003', 'IMPROVEMENT_SUGGEST',   'Предложение по улучшению',               'GENERAL',                 'Идеи и предложения по совершенствованию сервисов банка',              TRUE, NOW()),
('00000000-0000-0000-0007-000000000004', 'DOCS_REQUEST',          'Запрос документов / выписок',            'GENERAL',                 'Запрос справок, выписок и других документов',                         TRUE, NOW());
