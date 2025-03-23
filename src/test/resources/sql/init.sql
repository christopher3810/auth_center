-- roles 테이블 생성
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL
);

-- role_permissions 테이블 생성
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission VARCHAR(255) NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL
);

-- user_roles 테이블 생성
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- refresh_tokens 테이블 생성
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 기본 역할 추가
INSERT INTO roles (code, name, description, created_by, updated_by) VALUES
('ROLE_ADMIN', '관리자', '시스템 관리자 역할', 'SYSTEM', 'SYSTEM'),
('ROLE_USER', '일반 사용자', '일반 사용자 역할', 'SYSTEM', 'SYSTEM');

-- 기본 권한 추가
INSERT INTO role_permissions (role_id, permission) VALUES
((SELECT id FROM roles WHERE code = 'ROLE_ADMIN'), 'USER_READ'),
((SELECT id FROM roles WHERE code = 'ROLE_ADMIN'), 'USER_WRITE'),
((SELECT id FROM roles WHERE code = 'ROLE_ADMIN'), 'USER_DELETE'),
((SELECT id FROM roles WHERE code = 'ROLE_USER'), 'USER_READ'); 