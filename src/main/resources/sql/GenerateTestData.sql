-- =================================================================
-- TEST DATA GENERATION SCRIPT FOR HOSTE-O (POSTGRESQL)
-- =================================================================
-- This script uses a DO block to declare and use variables,
-- ensuring the entire script is self-contained and readable.
-- =================================================================

DO $$
DECLARE
    -- ---------------------------------
    -- CONFIGURATION VARIABLES
    -- ---------------------------------
    base_date date := '2026-04-02';
    user_id uuid := '06c7ab9a-f1da-4d5b-8d39-fc81fe3c0f0e';

    -- ---------------------------------
    -- ID DEFINITIONS
    -- ---------------------------------
   -- Apartments
      apt1_id uuid := 'a1a1a1a1-0001-4001-8001-000000000001';
      apt2_id uuid := 'a1a1a1a1-0002-4002-8002-000000000002';
      apt3_id uuid := 'a1a1a1a1-0003-4003-8003-000000000003';
      apt4_id uuid := 'a1a1a1a1-0004-4004-8004-000000000004';
      apt5_id uuid := 'a1a1a1a1-0005-4005-8005-000000000005';

      -- Tasks
      task1_1_id uuid := 'b1b1b1b1-0001-4001-8001-000000000001';
      task1_2_id uuid := 'b1b1b1b1-0001-4001-8001-000000000002';
      task1_3_id uuid := 'b1b1b1b1-0001-4001-8001-000000000003';
      task2_1_id uuid := 'b1b1b1b1-0002-4002-8002-000000000001';
      task2_2_id uuid := 'b1b1b1b1-0002-4002-8002-000000000002';
      task2_3_id uuid := 'b1b1b1b1-0002-4002-8002-000000000003';
      task3_1_id uuid := 'b1b1b1b1-0003-4003-8003-000000000001';
      task3_2_id uuid := 'b1b1b1b1-0003-4003-8003-000000000002';
      task3_3_id uuid := 'b1b1b1b1-0003-4003-8003-000000000003';
      task4_1_id uuid := 'b1b1b1b1-0004-4004-8004-000000000001';
      task4_2_id uuid := 'b1b1b1b1-0004-4004-8004-000000000002';
      task4_3_id uuid := 'b1b1b1b1-0004-4004-8004-000000000003';
      task5_1_id uuid := 'b1b1b1b1-0005-4005-8005-000000000001';
      task5_2_id uuid := 'b1b1b1b1-0005-4005-8005-000000000002';
      task5_3_id uuid := 'b1b1b1b1-0005-4005-8005-000000000003';

      -- Workers
      worker1_id uuid := 'c1c1c1c1-0001-4001-8001-000000000001';
      worker2_id uuid := 'c1c1c1c1-0002-4002-8002-000000000002';
      worker3_id uuid := 'c1c1c1c1-0003-4003-8003-000000000003';
      worker4_id uuid := 'c1c1c1c1-0004-4004-8004-000000000004';
      worker5_id uuid := 'c1c1c1c1-0005-4005-8005-000000000005';
      worker6_id uuid := 'c1c1c1c1-0006-4006-8006-000000000006';
      worker7_id uuid := 'c1c1c1c1-0007-4007-8007-000000000007';
      worker8_id uuid := 'c1c1c1c1-0008-4008-8008-000000000008';

      -- Events (Bookings)
      -- APT 1 events (7 bookings)
      event1_1_id uuid := 'e1e1e1e1-0001-4001-8001-000000000001';
      event1_2_id uuid := 'e1e1e1e1-0001-4001-8001-000000000002';
      event1_3_id uuid := 'e1e1e1e1-0001-4001-8001-000000000003';
      event1_4_id uuid := 'e1e1e1e1-0001-4001-8001-000000000004';
      event1_5_id uuid := 'e1e1e1e1-0001-4001-8001-000000000005';
      event1_6_id uuid := 'e1e1e1e1-0001-4001-8001-000000000006';
      event1_7_id uuid := 'e1e1e1e1-0001-4001-8001-000000000007';
      -- APT 2 events (8 bookings)
      event2_1_id uuid := 'e1e1e1e1-0002-4002-8002-000000000001';
      event2_2_id uuid := 'e1e1e1e1-0002-4002-8002-000000000002';
      event2_3_id uuid := 'e1e1e1e1-0002-4002-8002-000000000003';
      event2_4_id uuid := 'e1e1e1e1-0002-4002-8002-000000000004';
      event2_5_id uuid := 'e1e1e1e1-0002-4002-8002-000000000005';
      event2_6_id uuid := 'e1e1e1e1-0002-4002-8002-000000000006';
      event2_7_id uuid := 'e1e1e1e1-0002-4002-8002-000000000007';
      event2_8_id uuid := 'e1e1e1e1-0002-4002-8002-000000000008';
      -- APT 3 events (7 bookings)
      event3_1_id uuid := 'e1e1e1e1-0003-4003-8003-000000000001';
      event3_2_id uuid := 'e1e1e1e1-0003-4003-8003-000000000002';
      event3_3_id uuid := 'e1e1e1e1-0003-4003-8003-000000000003';
      event3_4_id uuid := 'e1e1e1e1-0003-4003-8003-000000000004';
      event3_5_id uuid := 'e1e1e1e1-0003-4003-8003-000000000005';
      event3_6_id uuid := 'e1e1e1e1-0003-4003-8003-000000000006';
      event3_7_id uuid := 'e1e1e1e1-0003-4003-8003-000000000007';
      -- APT 4 events (7 bookings)
      event4_1_id uuid := 'e1e1e1e1-0004-4004-8004-000000000001';
      event4_2_id uuid := 'e1e1e1e1-0004-4004-8004-000000000002';
      event4_3_id uuid := 'e1e1e1e1-0004-4004-8004-000000000003';
      event4_4_id uuid := 'e1e1e1e1-0004-4004-8004-000000000004';
      event4_5_id uuid := 'e1e1e1e1-0004-4004-8004-000000000005';
      event4_6_id uuid := 'e1e1e1e1-0004-4004-8004-000000000006';
      event4_7_id uuid := 'e1e1e1e1-0004-4004-8004-000000000007';
      -- APT 5 events (7 bookings)
      event5_1_id uuid := 'e1e1e1e1-0005-4005-8005-000000000001';
      event5_2_id uuid := 'e1e1e1e1-0005-4005-8005-000000000002';
      event5_3_id uuid := 'e1e1e1e1-0005-4005-8005-000000000003';
      event5_4_id uuid := 'e1e1e1e1-0005-4005-8005-000000000004';
      event5_5_id uuid := 'e1e1e1e1-0005-4005-8005-000000000005';
      event5_6_id uuid := 'e1e1e1e1-0005-4005-8005-000000000006';
      event5_7_id uuid := 'e1e1e1e1-0005-4005-8005-000000000007';


BEGIN
    -- ---------------------------------
    -- DATA DELETION
    -- ---------------------------------
    DELETE FROM assignments;
    DELETE FROM imp_bookings;
    DELETE FROM templates;
    DELETE FROM tasks;
    DELETE FROM events;
    DELETE FROM workers;
    DELETE FROM apartments;

    -- ---------------------------------
    -- DATA CREATION
    -- ---------------------------------

    -- Create 5 apartments
INSERT INTO apartments (id, name, booking_id, airbnb_id, address, state, visible, created_by, created_at) VALUES
(apt1_id, 'Sunny Downtown Loft', 'bk-apt-001', 'air-apt-001', '{"street": "123 Main St", "city": "Metropolis", "country": "USA"}', 'READY', true, user_id, NOW()),
(apt2_id, 'Cozy Garden Flat', 'bk-apt-002', 'air-apt-002', '{"street": "456 Oak Ave", "city": "Star City", "country": "USA"}', 'READY', true, user_id, NOW()),
(apt3_id, 'Modern Lakeside Villa', 'bk-apt-003', 'air-apt-003', '{"street": "789 Pine Ln", "city": "Gotham", "country": "USA"}', 'READY', true, user_id, NOW()),
(apt4_id, 'Rustic Mountain Cabin', 'bk-apt-004', 'air-apt-004', '{"street": "101 Mountain Rd", "city": "Central City", "country": "USA"}', 'READY', true, user_id, NOW()),
(apt5_id, 'Chic Urban Studio', 'bk-apt-005', 'air-apt-005', '{"street": "212 River Walk", "city": "Coast City", "country": "USA"}', 'READY', true, user_id, NOW());

    -- Create tasks for each apartment
    INSERT INTO tasks (id, name, category, duration, type, apartment_id, created_by, created_at) VALUES
    (task1_1_id, 'Standard Cleaning', 'CLEANING', 120, 'MANDATORY', apt1_id, user_id, NOW()),
    (task1_2_id, 'Check faucets', 'MAINTENANCE', 45, 'MANDATORY', apt1_id, user_id, NOW()),
    (task1_3_id, 'Restock Amenities', 'MAINTENANCE', 30, 'MANDATORY', apt1_id, user_id, NOW()),
    (task2_1_id, 'Full Cleaning', 'CLEANING', 150, 'MANDATORY', apt2_id, user_id, NOW()),
    (task2_2_id, 'Change Linens', 'CLEANING', 30, 'MANDATORY', apt2_id, user_id, NOW()),
    (task2_3_id, 'Check Wi-Fi', 'MAINTENANCE', 20, 'MANDATORY', apt2_id, user_id, NOW()),
    (task3_1_id, 'Post-Stay Cleaning', 'CLEANING', 120, 'MANDATORY', apt3_id, user_id, NOW()),
    (task3_2_id, 'Garden Tidy-Up', 'MAINTENANCE', 90, 'MANDATORY', apt3_id, user_id, NOW()),
    (task3_3_id, 'Guest Welcome Prep', 'MAINTENANCE', 45, 'MANDATORY', apt3_id, user_id, NOW()),
    (task4_1_id, 'Cabin Cleaning', 'CLEANING', 180, 'MANDATORY', apt4_id, user_id, NOW()),
    (task4_2_id, 'Firewood Restock', 'MAINTENANCE', 60, 'MANDATORY', apt4_id, user_id, NOW()),
    (task4_3_id, 'Hot Tub Maintenance', 'MAINTENANCE', 75, 'MANDATORY', apt4_id, user_id, NOW()),
    (task5_1_id, 'Studio Cleaning', 'CLEANING', 90, 'MANDATORY', apt5_id, user_id, NOW()),
    (task5_2_id, 'Appliance Check', 'MAINTENANCE', 40, 'MANDATORY', apt5_id, user_id, NOW()),
    (task5_3_id, 'Patio Cleaning', 'CLEANING', 60, 'MANDATORY', apt5_id, user_id, NOW());

    -- Create 8 workers (2 inactive)
    INSERT INTO workers (id, name, visible, created_by, created_at) VALUES
    (worker1_id, 'John Doe', true, user_id, NOW()),
    (worker2_id, 'Jane Smith', true, user_id, NOW()),
    (worker3_id, 'Peter Jones', true, user_id, NOW()),
    (worker4_id, 'Mary Williams', true, user_id, NOW()),
    (worker5_id, 'David Brown', true, user_id, NOW()),
    (worker6_id, 'Susan Davis', true, user_id, NOW()),
    (worker7_id, 'Robert Miller',  false, user_id, NOW()),
    (worker8_id, 'Linda Wilson',  false, user_id, NOW());

    -- Create Events (one per booking)
    -- APT 1 Events
    INSERT INTO events (id, type, name, source, state, apartment_id, start_date, end_date, created_by, created_at) VALUES
    (event1_1_id, 'BOOKING', 'Guest A', 'AIRBNB', 'FINISHED', apt1_id, base_date - INTERVAL '10 day', base_date - INTERVAL '7 day', user_id, NOW()),
    (event1_2_id, 'BOOKING', 'Guest B', 'BOOKING', 'FINISHED', apt1_id, base_date - INTERVAL '6 day', base_date - INTERVAL '4 day', user_id, NOW()),
    (event1_3_id, 'BOOKING', 'Guest C (Current)', 'NONE', 'IN_PROGRESS', apt1_id, base_date - INTERVAL '1 day', base_date + INTERVAL '2 day', user_id, NOW()),
    (event1_4_id, 'BOOKING', 'Guest D', 'AIRBNB', 'PENDING', apt1_id, base_date + INTERVAL '3 day', base_date + INTERVAL '5 day', user_id, NOW()),
    (event1_5_id, 'BOOKING', 'Guest E', 'NONE', 'PENDING', apt1_id, base_date + INTERVAL '6 day', base_date + INTERVAL '8 day', user_id, NOW()),
    (event1_6_id, 'BOOKING', 'Guest F', 'BOOKING', 'PENDING', apt1_id, base_date + INTERVAL '10 day', base_date + INTERVAL '12 day', user_id, NOW()),
    (event1_7_id, 'BOOKING', 'Guest G', 'NONE', 'PENDING', apt1_id, base_date + INTERVAL '14 day', base_date + INTERVAL '16 day', user_id, NOW());

    -- APT 2 Events
    INSERT INTO events (id, type, name, source, state, apartment_id, start_date, end_date, created_by, created_at) VALUES
    (event2_1_id, 'BOOKING', 'Guest H', 'BOOKING', 'FINISHED', apt2_id, base_date - INTERVAL '12 day', base_date - INTERVAL '10 day', user_id, NOW()),
    (event2_2_id, 'BOOKING', 'Guest I', 'NONE', 'FINISHED', apt2_id, base_date - INTERVAL '8 day', base_date - INTERVAL '6 day', user_id, NOW()),
    (event2_3_id, 'BOOKING', 'Guest J', 'AIRBNB', 'FINISHED', apt2_id, base_date - INTERVAL '5 day', base_date - INTERVAL '3 day', user_id, NOW()),
    (event2_4_id, 'BOOKING', 'Guest K (Just Left)', 'BOOKING', 'FINISHED', apt2_id, base_date - INTERVAL '2 day', base_date, user_id, NOW()),
    (event2_5_id, 'BOOKING', 'Guest L', 'NONE', 'PENDING', apt2_id, base_date + INTERVAL '2 day', base_date + INTERVAL '4 day', user_id, NOW()),
    (event2_6_id, 'BOOKING', 'Guest M', 'AIRBNB', 'PENDING', apt2_id, base_date + INTERVAL '5 day', base_date + INTERVAL '8 day', user_id, NOW()),
    (event2_7_id, 'BOOKING', 'Guest N', 'NONE', 'PENDING', apt2_id, base_date + INTERVAL '9 day', base_date + INTERVAL '11 day', user_id, NOW()),
    (event2_8_id, 'BOOKING', 'Guest O', 'BOOKING', 'PENDING', apt2_id, base_date + INTERVAL '13 day', base_date + INTERVAL '15 day', user_id, NOW());

    -- APT 3 Events
    INSERT INTO events (id, type, name, source, state, apartment_id, start_date, end_date, created_by, created_at) VALUES
    (event3_1_id, 'BOOKING', 'Guest P', 'NONE', 'FINISHED', apt3_id, base_date - INTERVAL '9 day', base_date - INTERVAL '7 day', user_id, NOW()),
    (event3_2_id, 'BOOKING', 'Guest Q', 'AIRBNB', 'FINISHED', apt3_id, base_date - INTERVAL '5 day', base_date - INTERVAL '3 day', user_id, NOW()),
    (event3_3_id, 'BOOKING', 'Guest R (Current)', 'BOOKING', 'IN_PROGRESS', apt3_id, base_date - INTERVAL '2 day', base_date + INTERVAL '3 day', user_id, NOW()),
    (event3_4_id, 'BOOKING', 'Guest S', 'NONE', 'PENDING', apt3_id, base_date + INTERVAL '4 day', base_date + INTERVAL '6 day', user_id, NOW()),
    (event3_5_id, 'BOOKING', 'Guest T', 'NONE', 'PENDING', apt3_id, base_date + INTERVAL '8 day', base_date + INTERVAL '10 day', user_id, NOW()),
    (event3_6_id, 'BOOKING', 'Guest U', 'AIRBNB', 'PENDING', apt3_id, base_date + INTERVAL '12 day', base_date + INTERVAL '14 day', user_id, NOW()),
    (event3_7_id, 'BOOKING', 'Guest V', 'BOOKING', 'PENDING', apt3_id, base_date + INTERVAL '15 day', base_date + INTERVAL '18 day', user_id, NOW());

    -- APT 4 Events
    INSERT INTO events (id, type, name, source, state, apartment_id, start_date, end_date, created_by, created_at) VALUES
    (event4_1_id, 'BOOKING', 'Guest W', 'AIRBNB', 'FINISHED', apt4_id, base_date - INTERVAL '15 day', base_date - INTERVAL '12 day', user_id, NOW()),
    (event4_2_id, 'BOOKING', 'Guest X', 'BOOKING', 'FINISHED', apt4_id, base_date - INTERVAL '10 day', base_date - INTERVAL '8 day', user_id, NOW()),
    (event4_3_id, 'BOOKING', 'Guest Y', 'NONE', 'FINISHED', apt4_id, base_date - INTERVAL '5 day', base_date - INTERVAL '3 day', user_id, NOW()),
    (event4_4_id, 'BOOKING', 'Guest Z', 'NONE', 'PENDING', apt4_id, base_date + INTERVAL '3 day', base_date + INTERVAL '5 day', user_id, NOW()),
    (event4_5_id, 'BOOKING', 'Guest AA', 'AIRBNB', 'PENDING', apt4_id, base_date + INTERVAL '7 day', base_date + INTERVAL '9 day', user_id, NOW()),
    (event4_6_id, 'BOOKING', 'Guest BB', 'BOOKING', 'PENDING', apt4_id, base_date + INTERVAL '11 day', base_date + INTERVAL '13 day', user_id, NOW()),
    (event4_7_id, 'BOOKING', 'Guest CC', 'NONE', 'PENDING', apt4_id, base_date + INTERVAL '15 day', base_date + INTERVAL '17 day', user_id, NOW());

    -- APT 5 Events
    INSERT INTO events (id, type, name, source, state, apartment_id, start_date, end_date, created_by, created_at) VALUES
    (event5_1_id, 'BOOKING', 'Guest DD', 'BOOKING', 'FINISHED', apt5_id, base_date - INTERVAL '11 day', base_date - INTERVAL '9 day', user_id, NOW()),
    (event5_2_id, 'BOOKING', 'Guest EE', 'NONE', 'FINISHED', apt5_id, base_date - INTERVAL '7 day', base_date - INTERVAL '5 day', user_id, NOW()),
    (event5_3_id, 'BOOKING', 'Guest FF (Just Left)', 'AIRBNB', 'FINISHED', apt5_id, base_date - INTERVAL '4 day', base_date - INTERVAL '1 day', user_id, NOW()),
    (event5_4_id, 'BOOKING', 'Guest GG', 'BOOKING', 'PENDING', apt5_id, base_date + INTERVAL '4 day', base_date + INTERVAL '6 day', user_id, NOW()),
    (event5_5_id, 'BOOKING', 'Guest HH', 'NONE', 'PENDING', apt5_id, base_date + INTERVAL '8 day', base_date + INTERVAL '10 day', user_id, NOW()),
    (event5_6_id, 'BOOKING', 'Guest II', 'AIRBNB', 'PENDING', apt5_id, base_date + INTERVAL '12 day', base_date + INTERVAL '15 day', user_id, NOW()),
    (event5_7_id, 'BOOKING', 'Guest JJ', 'NONE', 'PENDING', apt5_id, base_date + INTERVAL '16 day', base_date + INTERVAL '18 day', user_id, NOW());

    -- Update Apartment States based on the created bookings
    UPDATE apartments SET state = 'OCCUPIED' WHERE id = apt1_id;
    UPDATE apartments SET state = 'USED' WHERE id = apt2_id;
    UPDATE apartments SET state = 'OCCUPIED' WHERE id = apt3_id;
    UPDATE apartments SET state = 'READY' WHERE id = apt4_id;
    UPDATE apartments SET state = 'USED' WHERE id = apt5_id;

    -- Create FINISHED assignments for past bookings
    -- APT 1: Cleanup after Guest A (event1_1, checked out base_date-7) and Guest B (event1_2, checked out base_date-4)
    INSERT INTO assignments (id, task_id, event_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task1_1_id, event1_1_id, base_date - INTERVAL '7 day', base_date - INTERVAL '7 day' + INTERVAL '120 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_2_id, event1_1_id, base_date - INTERVAL '7 day' + INTERVAL '120 minute', base_date - INTERVAL '7 day' + INTERVAL '120 minute' + INTERVAL '45 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_3_id, event1_1_id, base_date - INTERVAL '7 day' + INTERVAL '120 minute' + INTERVAL '45 minute', base_date - INTERVAL '7 day' + INTERVAL '120 minute' + INTERVAL '45 minute' + INTERVAL '30 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_1_id, event1_2_id, base_date - INTERVAL '3 day', base_date - INTERVAL '3 day' + INTERVAL '120 minute', worker4_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_2_id, event1_2_id, base_date - INTERVAL '3 day' + INTERVAL '120 minute', base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '45 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task1_3_id, event1_2_id, base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '45 minute', base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '45 minute' + INTERVAL '30 minute', worker4_id, 'FINISHED', user_id, NOW());

    -- APT 2: Cleanup after Guest H (event2_1), Guest I (event2_2), Guest J (event2_3)
    INSERT INTO assignments (id, task_id, event_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task2_1_id, event2_1_id, base_date - INTERVAL '9 day', base_date - INTERVAL '9 day' + INTERVAL '150 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_2_id, event2_1_id, base_date - INTERVAL '9 day' + INTERVAL '150 minute', base_date - INTERVAL '9 day' + INTERVAL '150 minute' + INTERVAL '30 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_3_id, event2_1_id, base_date - INTERVAL '9 day' + INTERVAL '150 minute' + INTERVAL '30 minute', base_date - INTERVAL '9 day' + INTERVAL '150 minute' + INTERVAL '30 minute' + INTERVAL '20 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_1_id, event2_2_id, base_date - INTERVAL '6 day', base_date - INTERVAL '6 day' + INTERVAL '150 minute', worker4_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_2_id, event2_2_id, base_date - INTERVAL '6 day' + INTERVAL '150 minute', base_date - INTERVAL '6 day' + INTERVAL '150 minute' + INTERVAL '30 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_3_id, event2_2_id, base_date - INTERVAL '6 day' + INTERVAL '150 minute' + INTERVAL '30 minute', base_date - INTERVAL '6 day' + INTERVAL '150 minute' + INTERVAL '30 minute' + INTERVAL '20 minute', worker4_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_1_id, event2_3_id, base_date - INTERVAL '3 day', base_date - INTERVAL '3 day' + INTERVAL '150 minute', worker6_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_2_id, event2_3_id, base_date - INTERVAL '3 day' + INTERVAL '150 minute', base_date - INTERVAL '3 day' + INTERVAL '150 minute' + INTERVAL '30 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task2_3_id, event2_3_id, base_date - INTERVAL '3 day' + INTERVAL '150 minute' + INTERVAL '30 minute', base_date - INTERVAL '3 day' + INTERVAL '150 minute' + INTERVAL '30 minute' + INTERVAL '20 minute', worker6_id, 'FINISHED', user_id, NOW());

    -- APT 3: Cleanup after Guest P (event3_1), Guest Q (event3_2)
    INSERT INTO assignments (id, task_id, event_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task3_1_id, event3_1_id, base_date - INTERVAL '6 day', base_date - INTERVAL '6 day' + INTERVAL '120 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_2_id, event3_1_id, base_date - INTERVAL '6 day' + INTERVAL '120 minute', base_date - INTERVAL '6 day' + INTERVAL '120 minute' + INTERVAL '90 minute', worker3_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_3_id, event3_1_id, base_date - INTERVAL '6 day' + INTERVAL '120 minute' + INTERVAL '90 minute', base_date - INTERVAL '6 day' + INTERVAL '120 minute' + INTERVAL '90 minute' + INTERVAL '45 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_1_id, event3_2_id, base_date - INTERVAL '3 day', base_date - INTERVAL '3 day' + INTERVAL '120 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_2_id, event3_2_id, base_date - INTERVAL '3 day' + INTERVAL '120 minute', base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '90 minute', worker6_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task3_3_id, event3_2_id, base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '90 minute', base_date - INTERVAL '3 day' + INTERVAL '120 minute' + INTERVAL '90 minute' + INTERVAL '45 minute', worker5_id, 'FINISHED', user_id, NOW());

    -- APT 4: Cleanup after Guest W (event4_1), Guest X (event4_2)
    INSERT INTO assignments (id, task_id, event_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task4_1_id, event4_1_id, base_date - INTERVAL '11 day', base_date - INTERVAL '11 day' + INTERVAL '180 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_2_id, event4_1_id, base_date - INTERVAL '11 day' + INTERVAL '180 minute', base_date - INTERVAL '11 day' + INTERVAL '180 minute' + INTERVAL '60 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_3_id, event4_1_id, base_date - INTERVAL '11 day' + INTERVAL '180 minute' + INTERVAL '60 minute', base_date - INTERVAL '11 day' + INTERVAL '180 minute' + INTERVAL '60 minute' + INTERVAL '75 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_1_id, event4_2_id, base_date - INTERVAL '7 day', base_date - INTERVAL '7 day' + INTERVAL '180 minute', worker3_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_2_id, event4_2_id, base_date - INTERVAL '7 day' + INTERVAL '180 minute', base_date - INTERVAL '7 day' + INTERVAL '180 minute' + INTERVAL '60 minute', worker4_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_3_id, event4_2_id, base_date - INTERVAL '7 day' + INTERVAL '180 minute' + INTERVAL '60 minute', base_date - INTERVAL '7 day' + INTERVAL '180 minute' + INTERVAL '60 minute' + INTERVAL '75 minute', worker3_id, 'FINISHED', user_id, NOW());

    -- APT 5: Cleanup after Guest DD (event5_1), Guest EE (event5_2)
    INSERT INTO assignments (id, task_id, event_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task5_1_id, event5_1_id, base_date - INTERVAL '8 day', base_date - INTERVAL '8 day' + INTERVAL '90 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_2_id, event5_1_id, base_date - INTERVAL '8 day' + INTERVAL '90 minute', base_date - INTERVAL '8 day' + INTERVAL '90 minute' + INTERVAL '40 minute', worker6_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_3_id, event5_1_id, base_date - INTERVAL '8 day' + INTERVAL '90 minute' + INTERVAL '40 minute', base_date - INTERVAL '8 day' + INTERVAL '90 minute' + INTERVAL '40 minute' + INTERVAL '60 minute', worker5_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_1_id, event5_2_id, base_date - INTERVAL '5 day', base_date - INTERVAL '5 day' + INTERVAL '90 minute', worker1_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_2_id, event5_2_id, base_date - INTERVAL '5 day' + INTERVAL '90 minute', base_date - INTERVAL '5 day' + INTERVAL '90 minute' + INTERVAL '40 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task5_3_id, event5_2_id, base_date - INTERVAL '5 day' + INTERVAL '90 minute' + INTERVAL '40 minute', base_date - INTERVAL '5 day' + INTERVAL '90 minute' + INTERVAL '40 minute' + INTERVAL '60 minute', worker1_id, 'FINISHED', user_id, NOW());


    -- Create assignments to justify the CURRENT state of the system
    -- Apt 2 (USED): Create PENDING assignments for all its tasks for today (cleanup after Guest K, event2_4).
    INSERT INTO assignments (id, task_id, event_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task2_1_id, event2_4_id, base_date, base_date + INTERVAL '150 minute', worker1_id, 'PENDING', user_id, NOW()),
    (gen_random_uuid(), task2_2_id, event2_4_id, base_date + INTERVAL '150 minute', base_date + INTERVAL '150 minute' + INTERVAL '30 minute', worker2_id, 'PENDING', user_id, NOW()),
    (gen_random_uuid(), task2_3_id, event2_4_id, base_date + INTERVAL '150 minute' + INTERVAL '30 minute', base_date + INTERVAL '150 minute' + INTERVAL '30 minute' + INTERVAL '20 minute', worker1_id, 'PENDING', user_id, NOW());

    -- Apt 4 (READY): Create FINISHED assignments for all tasks for yesterday (cleanup after Guest Y, event4_3).
    INSERT INTO assignments (id, task_id, event_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task4_1_id, event4_3_id, base_date - INTERVAL '1 day', base_date - INTERVAL '1 day' + INTERVAL '180 minute', worker2_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_2_id, event4_3_id, base_date - INTERVAL '1 day' + INTERVAL '180 minute', base_date - INTERVAL '1 day' + INTERVAL '180 minute' + INTERVAL '60 minute', worker3_id, 'FINISHED', user_id, NOW()),
    (gen_random_uuid(), task4_3_id, event4_3_id, base_date - INTERVAL '1 day' + INTERVAL '180 minute' + INTERVAL '60 minute', base_date - INTERVAL '1 day' + INTERVAL '180 minute' + INTERVAL '60 minute' + INTERVAL '75 minute', worker2_id, 'FINISHED', user_id, NOW());

    -- Apt 5 (USED): PENDING assignments (cleanup after Guest FF, event5_3).
    INSERT INTO assignments (id, task_id, event_id, start_date, end_date, worker_id, state, created_by, created_at) VALUES
    (gen_random_uuid(), task5_1_id, event5_3_id, base_date, base_date + INTERVAL '90 minute', worker3_id, 'PENDING', user_id, NOW()),
    (gen_random_uuid(), task5_2_id, event5_3_id, base_date + INTERVAL '90 minute', base_date + INTERVAL '90 minute' + INTERVAL '40 minute', worker4_id, 'PENDING', user_id, NOW()),
    (gen_random_uuid(), task5_3_id, event5_3_id, base_date + INTERVAL '90 minute' + INTERVAL '40 minute', base_date + INTERVAL '90 minute' + INTERVAL '40 minute' + INTERVAL '60 minute', worker4_id, 'PENDING', user_id, NOW());

END $$;