-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    refresh_token VARCHAR(500)
);

-- Create menus table
CREATE TABLE IF NOT EXISTS menus (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(255),
    icon VARCHAR(255),
    parent_id BIGINT,
    menu_order INTEGER,
    menu_type VARCHAR(100),
    required_role VARCHAR(100),
    component VARCHAR(255)
);

-- Create items table
CREATE TABLE IF NOT EXISTS item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    recipient_id VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    read_at TIMESTAMP WITHOUT TIME ZONE
);

-- Add foreign key constraints
ALTER TABLE menus 
    ADD CONSTRAINT fk_menus_parent 
    FOREIGN KEY (parent_id) 
    REFERENCES menus(id) 
    ON DELETE SET NULL;

-- Add comments for users table
COMMENT ON TABLE users IS '사용자 정보를 저장하는 테이블';
COMMENT ON COLUMN users.id IS '사용자 고유 ID';
COMMENT ON COLUMN users.username IS '사용자 이름';
COMMENT ON COLUMN users.password IS '비밀번호 (암호화되어 저장됨)';
COMMENT ON COLUMN users.role IS '사용자 역할 (예: ROLE_USER, ROLE_ADMIN)';
COMMENT ON COLUMN users.refresh_token IS '리프레시 토큰';

-- Add comments for menus table
COMMENT ON TABLE menus IS '메뉴 정보를 저장하는 테이블';
COMMENT ON COLUMN menus.id IS '메뉴 고유 ID';
COMMENT ON COLUMN menus.name IS '메뉴 이름';
COMMENT ON COLUMN menus.path IS '메뉴 경로';
COMMENT ON COLUMN menus.icon IS '메뉴 아이콘';
COMMENT ON COLUMN menus.parent_id IS '상위 메뉴 ID';
COMMENT ON COLUMN menus.menu_order IS '메뉴 표시 순서';
COMMENT ON COLUMN menus.required_role IS '메뉴 접근에 필요한 권한';
COMMENT ON COLUMN menus.component IS '메뉴 컴포넌트 경로';

-- Add comments for item table
COMMENT ON TABLE item IS '아이템 정보를 저장하는 테이블';
COMMENT ON COLUMN item.id IS '아이템 고유 ID';
COMMENT ON COLUMN item.name IS '아이템 이름';
COMMENT ON COLUMN item.description IS '아이템 설명';

-- Add comments for notifications table
COMMENT ON TABLE notifications IS '알림 정보를 저장하는 테이블';
COMMENT ON COLUMN notifications.id IS '알림 고유 ID';
COMMENT ON COLUMN notifications.title IS '알림 제목';
COMMENT ON COLUMN notifications.message IS '알림 내용';
COMMENT ON COLUMN notifications.type IS '알림 유형 (info, success, warning, error)';
COMMENT ON COLUMN notifications.recipient_id IS '수신자 ID';
COMMENT ON COLUMN notifications.is_read IS '읽음 여부';
COMMENT ON COLUMN notifications.created_at IS '생성 일시';
COMMENT ON COLUMN notifications.read_at IS '읽은 일시';

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_menus_parent_id ON menus(parent_id);
CREATE INDEX IF NOT EXISTS idx_menus_menu_order ON menus(menu_order);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, role) 
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Insert default menu items
INSERT INTO menus (id, icon, menu_order, menu_type, name, parent_id, path, required_role, component) VALUES
(1, 'dashboard', 10, NULL, '대시보드', NULL, '/dashboard', NULL, 'views/dashboard/Dashboard.vue'),
(2, 'inventory_2', 20, NULL, '상품', NULL, '/products', NULL, 'views/product/ProductsPage.vue'),
(3, 'manage_accounts', 30, NULL, '유저관리', NULL, '/users', 'ROLE_ADMIN', NULL)
ON CONFLICT (id) DO UPDATE SET
    icon = EXCLUDED.icon,
    menu_order = EXCLUDED.menu_order,
    menu_type = EXCLUDED.menu_type,
    name = EXCLUDED.name,
    parent_id = EXCLUDED.parent_id,
    path = EXCLUDED.path,
    required_role = EXCLUDED.required_role,
    component = EXCLUDED.component;
