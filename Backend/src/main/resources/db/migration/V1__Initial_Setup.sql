-- V1__init.sql

-- ==============================
-- Create 'candle' Table
-- ==============================
-- DROP TABLE IF EXISTS candle;
CREATE TABLE IF NOT EXISTS candle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    symbol VARCHAR(50) NOT NULL,
    open_time DATETIME NOT NULL,
    open_price DOUBLE NOT NULL,
    high_price DOUBLE NOT NULL,
    low_price DOUBLE NOT NULL,
    close_price DOUBLE NOT NULL,
    volume DOUBLE NOT NULL,
    formatted_time VARCHAR(50),
    date DATE NOT NULL,
    formatted_open_time VARCHAR(50)
);

-- ==============================
-- Create 'instructor' Table
-- ==============================
CREATE TABLE IF NOT EXISTS instructor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) DEFAULT 'haven\'t finished KYC',
    last_name VARCHAR(255) DEFAULT 'haven\'t finished KYC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    user_id VARCHAR(255) UNIQUE,
    instructor_number VARCHAR(50)
);

-- ==============================
-- Create 'trader' Table
-- ==============================
CREATE TABLE IF NOT EXISTS trader (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) DEFAULT 'haven\'t finished KYC',
    last_name VARCHAR(255) DEFAULT 'haven\'t finished KYC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    user_id VARCHAR(255) UNIQUE,

    usdt_balance DECIMAL(10, 2) DEFAULT 0.00 NOT NULL,
    id_number VARCHAR(50),
    phone_number VARCHAR(50),
    yesterday_price DECIMAL(20, 2) DEFAULT 0.00
);

-- ==============================
-- Create 'wallet' Table
-- ==============================
CREATE TABLE IF NOT EXISTS wallet (
   id INT PRIMARY KEY AUTO_INCREMENT,
   trader_id BIGINT NOT NULL,
   currency VARCHAR(10) NOT NULL,
   amount DECIMAL(20, 8) DEFAULT 0.00 NOT NULL,

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

   CONSTRAINT fk_wallet_trader
       FOREIGN KEY(trader_id)
           REFERENCES trader(id)
           ON DELETE CASCADE,

   CONSTRAINT uc_wallet_unique
       UNIQUE(trader_id, currency)
);

-- ==============================
-- Create 'orders' Table
-- ==============================
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    trader_id BIGINT NOT NULL,
    buy_and_sell_type VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount DECIMAL(20, 8) NOT NULL,
    price DECIMAL(20, 8) NOT NULL,
    market_or_limit_order_types VARCHAR(20) NOT NULL,
    order_status VARCHAR(20) DEFAULT 'Pending' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_order_trader
        FOREIGN KEY(trader_id)
            REFERENCES trader(id)
            ON DELETE CASCADE
);



-- ==============================
-- Create 'transactions' Table
-- ==============================
CREATE TABLE IF NOT EXISTS transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    trader_id BIGINT NOT NULL,
    wallet_id INT NOT NULL,
    order_id INT NOT NULL,
    buy_or_sell_type VARCHAR(20) NOT NULL,
    price DECIMAL(20, 8) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount DECIMAL(20, 8) NOT NULL,

    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_transaction_trader
        FOREIGN KEY(trader_id)
            REFERENCES trader(id)
            ON DELETE CASCADE,

   CONSTRAINT fk_transaction_wallet
        FOREIGN KEY(wallet_id)
            REFERENCES wallet(id)
            ON DELETE CASCADE,

   CONSTRAINT fk_transaction_order
        FOREIGN KEY(order_id)
            REFERENCES orders(id)
            ON DELETE CASCADE
);

