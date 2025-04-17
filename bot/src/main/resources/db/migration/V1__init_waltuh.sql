-- ======================================
-- SCHEMA INITIALIZATION FOR STAMP SYSTEM
-- ======================================

-- 1. Create Stamp Table
CREATE TABLE stamp (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    quantity INTEGER,
    value NUMERIC(10, 2) NOT NULL
);

-- 2. Create Stamp Configuration Table
CREATE TABLE stamp_configuration (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL
);

-- 3. Create Stamp Combination Table (Join Table)
CREATE TABLE stamp_combination (
    stamp_configuration_id UUID NOT NULL,
    stamp_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_stamp_combination_stamp FOREIGN KEY (stamp_id)
        REFERENCES stamp(id) ON DELETE CASCADE,
    CONSTRAINT fk_combination_config FOREIGN KEY (stamp_configuration_id)
        REFERENCES stamp_configuration(id) ON DELETE CASCADE
);

-- 4. Create Envelope Table
CREATE TABLE envelope (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    quantity INTEGER,
    price NUMERIC(10, 2),
    stamp_config_id UUID,
    CONSTRAINT fk_envelope_stamp_configuration FOREIGN KEY (stamp_config_id)
        REFERENCES stamp_configuration(id)
);

-- ======================================
-- SAMPLE DATA
-- ======================================

-- Insert Stamp
INSERT INTO stamp (id, name, description, quantity, value)
VALUES
  ('00000000-0000-0000-0000-000000000150', '150c', 'One dollar fifty cent stamp', NULL, 1.50);

-- Insert Stamp Configurations
INSERT INTO stamp_configuration (id, name) VALUES
  ('11111111-1111-1111-1111-111111111c04', 'C4'),
  ('11111111-1111-1111-1111-111111111c05', 'C5'),
  ('11111111-1111-1111-1111-111111111c00', 'Small');

-- Insert Stamp Combinations
INSERT INTO stamp_combination (stamp_configuration_id, stamp_id, quantity) VALUES
  ('11111111-1111-1111-1111-111111111c04', '00000000-0000-0000-0000-000000000150', 2),
  ('11111111-1111-1111-1111-111111111c05', '00000000-0000-0000-0000-000000000150', 2),
  ('11111111-1111-1111-1111-111111111c00', '00000000-0000-0000-0000-000000000150', 1);

-- Insert Envelopes
INSERT INTO envelope (id, name, quantity, price, description, stamp_config_id)
VALUES
  (gen_random_uuid(), 'C4', 100, 5.00, 'C4 Envelope', '11111111-1111-1111-1111-111111111c04'),
  (gen_random_uuid(), 'C5', 1000, 4.00, 'C5 Envelope', '11111111-1111-1111-1111-111111111c05'),
  (gen_random_uuid(), 'Small', 123, 3.00, 'Small Envelope', '11111111-1111-1111-1111-111111111c00'),
  (gen_random_uuid(), 'TMP-Small', 199, 3.00, 'TRACKING Small Envelope', null);
