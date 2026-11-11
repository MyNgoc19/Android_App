-- ROOM_RENTAL_MANAGEMENT_SYSTEM
-- Shared SQLite schema for Android User + Web Admin + FastAPI.
-- Run with: sqlite3 room_rental.db < schema.sql

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    firebase_uid    TEXT NOT NULL UNIQUE,
    email           TEXT NOT NULL UNIQUE,
    full_name       TEXT NOT NULL,
    phone           TEXT,
    avatar_url      TEXT,
    role            TEXT NOT NULL DEFAULT 'USER'
                    CHECK (role IN ('USER', 'ADMIN')),
    status          TEXT NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'BLOCKED')),
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- A room is one rental listing/room available for rent.
CREATE TABLE IF NOT EXISTS rooms (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    title           TEXT NOT NULL,
    description     TEXT,
    address         TEXT NOT NULL,
    ward            TEXT,
    district        TEXT,
    city            TEXT NOT NULL,
    monthly_price   INTEGER NOT NULL CHECK (monthly_price >= 0),
    area_m2         REAL CHECK (area_m2 IS NULL OR area_m2 > 0),
    capacity        INTEGER NOT NULL DEFAULT 1 CHECK (capacity > 0),
    amenities       TEXT, -- JSON array stored as text, e.g. ["wifi","parking"]
    status          TEXT NOT NULL DEFAULT 'AVAILABLE'
                    CHECK (status IN ('AVAILABLE', 'RENTED', 'INACTIVE')),
    created_by      INTEGER NOT NULL,
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS room_images (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    room_id         INTEGER NOT NULL,
    image_url       TEXT NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- User submits a request to rent/view a room.
CREATE TABLE IF NOT EXISTS rental_requests (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    room_id         INTEGER NOT NULL,
    user_id         INTEGER NOT NULL,
    message         TEXT,
    desired_move_in TEXT, -- ISO date YYYY-MM-DD
    status          TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS rental_contracts (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    request_id      INTEGER NOT NULL UNIQUE,
    room_id         INTEGER NOT NULL,
    tenant_id       INTEGER NOT NULL,
    start_date      TEXT NOT NULL,
    end_date        TEXT,
    monthly_price   INTEGER NOT NULL CHECK (monthly_price >= 0),
    deposit_amount  INTEGER NOT NULL DEFAULT 0 CHECK (deposit_amount >= 0),
    status          TEXT NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'ENDED', 'CANCELLED')),
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES rental_requests(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (tenant_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS payments (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    contract_id     INTEGER NOT NULL,
    amount          INTEGER NOT NULL CHECK (amount > 0),
    payment_type    TEXT NOT NULL
                    CHECK (payment_type IN ('DEPOSIT', 'RENT', 'OTHER')),
    payment_period  TEXT, -- e.g. 2026-11 for monthly rent
    status          TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    paid_at         TEXT,
    note            TEXT,
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES rental_contracts(id)
);

CREATE TABLE IF NOT EXISTS favorites (
    user_id         INTEGER NOT NULL,
    room_id         INTEGER NOT NULL,
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, room_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    actor_id        INTEGER,
    action          TEXT NOT NULL,
    target_type     TEXT,
    target_id       INTEGER,
    details         TEXT,
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_rooms_status_city
    ON rooms(status, city);
CREATE INDEX IF NOT EXISTS idx_rooms_price
    ON rooms(monthly_price);
CREATE INDEX IF NOT EXISTS idx_requests_user
    ON rental_requests(user_id, status);
CREATE INDEX IF NOT EXISTS idx_requests_room
    ON rental_requests(room_id, status);
CREATE INDEX IF NOT EXISTS idx_contracts_tenant
    ON rental_contracts(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_payments_contract
    ON payments(contract_id, status);
