CREATE TABLE `users` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `name` varchar(100) NOT NULL,
  `email` varchar(150) UNIQUE NOT NULL,
  `password` varchar(255) NOT NULL,
  `current_routine` uuid COMMENT 'Rutina/Split actualmente en curso',
  `active_sequence_index` int DEFAULT 1 COMMENT 'Puntero al order_index que toca hoy (1, 2, 3...)',
  `created_at` timestamp DEFAULT (now())
);

CREATE TABLE `routine` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid NOT NULL,
  `title` varchar(100) NOT NULL COMMENT 'Ej: PPL 4 Días con Enfoque Dorsal',
  `description` text,
  `is_active` boolean DEFAULT false COMMENT 'Solo un split activo a la vez',
  `created_at` timestamp DEFAULT (now())
);

CREATE TABLE `workout` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `routine_id` uuid NOT NULL,
  `name` varchar(100) NOT NULL COMMENT 'Ej: Jale A (Dorsal), Empuje A, Pierna',
  `order_index` int NOT NULL COMMENT 'Orden rotativo de ejecución cíclica',
  `created_at` timestamp DEFAULT (now())
);

CREATE TABLE `workout_exercises` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `workout_id` uuid NOT NULL,
  `exercise_id` uuid NOT NULL,
  `order_in_routine` int NOT NULL COMMENT '1º Press banca, 2º Fondos...',
  `rest_seconds` int DEFAULT 90 COMMENT 'Segundos para el timer de descanso GLOBAL (con menor prioridad que el definido en sets)',
  `notes` text
);

CREATE TABLE `workout_sets` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `work_exercise_id` uuid NOT NULL,
  `set_order` int NOT NULL COMMENT 'Define el numero de set del ejercicio',
  `type` ENUM ('NORMAL', 'WARNUP', 'DROPSET', 'FAILURE') NOT NULL DEFAULT 'NORMAL',
  `range_rep_ini` int NOT NULL,
  `range_rep_end` int NOT NULL,
  `target_rpe` numeric(3,1) COMMENT 'Esfuerzo estimado (ej: 8.5)',
  `rest_second` int COMMENT 'Segundos de descanso especificado para este set (con mayor prioridad vs global)'
);

CREATE TABLE `exercises` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid COMMENT 'NULL = Catálogo precargado; Con ID = Ejercicio propio',
  `external_id` varchar(50) UNIQUE COMMENT 'exerciseId del JSON de ExerciseDB (01qpYSe)',
  `name` varchar(150) NOT NULL,
  `gif_url` varchar(255) COMMENT 'Path local descargado o URL remota',
  `instructions` jsonb COMMENT 'Array de pasos ["Step:1 ...", "Step:2 ..."]',
  `created_at` timestamp DEFAULT (now())
);

CREATE TABLE `body_parts` (
  `id` serial PRIMARY KEY,
  `name` varchar(50) UNIQUE NOT NULL COMMENT 'chest, back, waist, upper arms...'
);

CREATE TABLE `muscles` (
  `id` serial PRIMARY KEY,
  `name` varchar(80) UNIQUE NOT NULL COMMENT 'latissimus dorsi, biceps, quadriceps...'
);

CREATE TABLE `equipments` (
  `id` serial PRIMARY KEY,
  `name` varchar(80) UNIQUE NOT NULL COMMENT 'barbell, dumbbell, cable, body weight...'
);

CREATE TABLE `exercise_body_parts` (
  `exercise_id` uuid NOT NULL,
  `body_part_id` int NOT NULL,
  PRIMARY KEY (`exercise_id`, `body_part_id`)
);

CREATE TABLE `exercise_equipments` (
  `exercise_id` uuid NOT NULL,
  `equipment_id` int NOT NULL,
  PRIMARY KEY (`exercise_id`, `equipment_id`)
);

CREATE TABLE `exercise_muscles` (
  `exercise_id` uuid NOT NULL,
  `muscle_id` int NOT NULL,
  `is_target` boolean DEFAULT true COMMENT 'true: targetMuscle, false: secondaryMuscle',
  PRIMARY KEY (`exercise_id`, `muscle_id`, `is_target`)
);

CREATE TABLE `exercise_substitutes` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `exercise_id` uuid NOT NULL COMMENT 'Ejercicio original',
  `substitute_exercise_id` uuid NOT NULL COMMENT 'Alternativa si máquina ocupada',
  `notes` varchar(255) COMMENT 'Ej: Si la polea alta está ocupada'
);

CREATE TABLE `workout_logs` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid NOT NULL,
  `workout_id` uuid NOT NULL COMMENT 'Día de la secuencia ejecutado',
  `title` varchar(100) NOT NULL COMMENT 'Ej: Sesión de Empuje - 15 Oct',
  `started_at` timestamp NOT NULL,
  `finished_at` timestamp,
  `overall_rpe` int COMMENT 'Fatiga general de la sesión (1-10)',
  `notes` text
);

CREATE TABLE `workout_log_sets` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `workout_log_id` uuid NOT NULL,
  `exercise_id` uuid NOT NULL,
  `set_order` int NOT NULL COMMENT 'Serie 1, 2, 3...',
  `set_type` ENUM ('NORMAL', 'WARNUP', 'DROPSET', 'FAILURE') DEFAULT 'NORMAL' COMMENT 'normal, warmup, dropset, failure',
  `weight_input` numeric(6,2) NOT NULL COMMENT 'Valor exacto introducido (ej: 45.0)',
  `weight_unit` ENUM ('KG', 'LBS') DEFAULT 'KG' COMMENT 'Unidad con la que se registró (KG o LBS)',
  `weight_kg` numeric(6,2) NOT NULL COMMENT 'Valor normalizado a kg para cálculos y analíticas',
  `reps_completed` int NOT NULL,
  `rpe` numeric(3,1) COMMENT 'RPE específico de la serie',
  `calculated_1rm` numeric(6,2) COMMENT 'Calculado siempre sobre weight_kg',
  `is_personal_record` boolean DEFAULT false,
  `created_at` timestamp DEFAULT (now())
);

CREATE TABLE `body_measurement` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid NOT NULL,
  `log_date` date NOT NULL,
  `weight_kg` numeric(5,2) NOT NULL COMMENT 'Peso en ayunas',
  `body_fat_percentage` numeric(4,2),
  `neck_cm` numeric(5,2),
  `chest_cm` numeric(5,2),
  `waist_navel_cm` numeric(5,2) COMMENT 'Cintura a la altura del ombligo',
  `hips_cm` numeric(5,2),
  `bicep_left_cm` numeric(5,2),
  `bicep_right_cm` numeric(5,2),
  `thigh_left_cm` numeric(5,2),
  `thigh_right_cm` numeric(5,2),
  `calf_cm` numeric(5,2),
  `notes` text
);

CREATE TABLE `progress_photos` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `measurement_id` uuid NOT NULL,
  `pose_type` ENUM ('FRONT', 'BACK', 'LEFT', 'RIGHT') NOT NULL COMMENT 'front, back, left, right',
  `photo_path` varchar(255) NOT NULL,
  `taken_at` timestamp DEFAULT (now())
);

CREATE TABLE `nutrition_targets` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid NOT NULL,
  `name` varchar(50) NOT NULL COMMENT 'Día de Entreno, Fin de Semana Flexible...',
  `calories_kcal` numeric(6,1) NOT NULL,
  `protein_g` numeric(5,1) NOT NULL,
  `carbs_g` numeric(5,1) NOT NULL,
  `fat_g` numeric(5,1) NOT NULL,
  `is_default` boolean DEFAULT false
);

CREATE TABLE `food_items` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid COMMENT 'NULL = Open Food Facts; Con ID = Alimento custom',
  `barcode` varchar(60) UNIQUE COMMENT 'Escaneo con cámara de Android',
  `name` varchar(200) NOT NULL,
  `brand` varchar(100),
  `serving_size_g` numeric(6,2) DEFAULT 100,
  `calories_per_100g` numeric(6,1) NOT NULL,
  `protein_per_100g` numeric(5,1) NOT NULL,
  `carbs_per_100g` numeric(5,1) NOT NULL,
  `fat_per_100g` numeric(5,1) NOT NULL,
  `fiber_per_100g` numeric(5,1) DEFAULT 0,
  `created_at` timestamp DEFAULT (now())
);

CREATE TABLE `recipes` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid NOT NULL,
  `name` varchar(150) NOT NULL COMMENT 'Ej: Desayuno Habitual, Batido Post-Entreno',
  `description` text
);

CREATE TABLE `recipe_items` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `recipe_id` uuid NOT NULL,
  `food_item_id` uuid NOT NULL,
  `amount_in_grams` numeric(6,2) NOT NULL COMMENT 'Gramos del ingrediente'
);

CREATE TABLE `daily_food_logs` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid NOT NULL,
  `food_item_id` uuid NOT NULL COMMENT 'Alimento individual',
  `recipe_id` uuid NOT NULL COMMENT 'O receta completa',
  `log_date` date NOT NULL,
  `meal_type` varchar(20) NOT NULL COMMENT 'breakfast, lunch, dinner, snack',
  `amount_in_grams` numeric(6,2) NOT NULL,
  `calculated_calories` numeric(6,1) NOT NULL,
  `calculated_protein` numeric(5,1) NOT NULL,
  `calculated_carbs` numeric(5,1) NOT NULL,
  `calculated_fat` numeric(5,1) NOT NULL,
  `created_at` timestamp DEFAULT (now())
);

CREATE TABLE `daily_habit_logs` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid NOT NULL,
  `log_date` date NOT NULL,
  `sleep_hours` numeric(4,2),
  `sleep_quality` int COMMENT 'Escala 1 al 5',
  `energy_level` int COMMENT 'Escala 1 al 5',
  `water_intake_ml` int DEFAULT 0,
  `water_target_ml` int DEFAULT 3000
);

CREATE TABLE `supplements` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `user_id` uuid NOT NULL,
  `name` varchar(100) NOT NULL COMMENT 'Ej: Creatina Monohidratada, Omega 3',
  `target_dosage` varchar(50) COMMENT 'Ej: 5g, 2 cápsulas'
);

CREATE TABLE `daily_supplement_logs` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `daily_habit_log_id` uuid NOT NULL,
  `supplement_id` uuid NOT NULL,
  `is_taken` boolean DEFAULT false,
  `taken_at` timestamp
);

CREATE TABLE `joint_discomfort_logs` (
  `id` uuid PRIMARY KEY DEFAULT (uuid_generate_v4()),
  `daily_habit_log_id` uuid NOT NULL,
  `joint_area` varchar(50) NOT NULL COMMENT 'shoulder_left, knee_right, lumbar...',
  `pain_intensity` int NOT NULL COMMENT 'Escala 1 al 10',
  `notes` varchar(255) COMMENT 'Ej: Pinchazo en press militar'
);

CREATE UNIQUE INDEX `workout_index_0` ON `workout` (`routine_id`, `order_index`);

CREATE UNIQUE INDEX `daily_habit_logs_index_1` ON `daily_habit_logs` (`user_id`, `log_date`);

ALTER TABLE `users` ADD FOREIGN KEY (`current_routine`) REFERENCES `routine` (`id`);

ALTER TABLE `routine` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `workout` ADD FOREIGN KEY (`routine_id`) REFERENCES `routine` (`id`);

ALTER TABLE `workout_exercises` ADD FOREIGN KEY (`workout_id`) REFERENCES `workout` (`id`);

ALTER TABLE `workout_exercises` ADD FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`);

ALTER TABLE `workout_sets` ADD FOREIGN KEY (`work_exercise_id`) REFERENCES `workout_exercises` (`id`);

ALTER TABLE `exercises` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `exercise_body_parts` ADD FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`);

ALTER TABLE `exercise_body_parts` ADD FOREIGN KEY (`body_part_id`) REFERENCES `body_parts` (`id`);

ALTER TABLE `exercise_equipments` ADD FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`);

ALTER TABLE `exercise_equipments` ADD FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`id`);

ALTER TABLE `exercise_muscles` ADD FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`);

ALTER TABLE `exercise_muscles` ADD FOREIGN KEY (`muscle_id`) REFERENCES `muscles` (`id`);

ALTER TABLE `exercise_substitutes` ADD FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`);

ALTER TABLE `exercise_substitutes` ADD FOREIGN KEY (`substitute_exercise_id`) REFERENCES `exercises` (`id`);

ALTER TABLE `workout_logs` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `workout_logs` ADD FOREIGN KEY (`workout_id`) REFERENCES `workout` (`id`);

ALTER TABLE `workout_log_sets` ADD FOREIGN KEY (`workout_log_id`) REFERENCES `workout_logs` (`id`);

ALTER TABLE `workout_log_sets` ADD FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`);

ALTER TABLE `body_measurement` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `progress_photos` ADD FOREIGN KEY (`measurement_id`) REFERENCES `body_measurement` (`id`);

ALTER TABLE `nutrition_targets` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `food_items` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `recipes` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `recipe_items` ADD FOREIGN KEY (`recipe_id`) REFERENCES `recipes` (`id`);

ALTER TABLE `recipe_items` ADD FOREIGN KEY (`food_item_id`) REFERENCES `food_items` (`id`);

ALTER TABLE `daily_food_logs` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `daily_food_logs` ADD FOREIGN KEY (`food_item_id`) REFERENCES `food_items` (`id`);

ALTER TABLE `daily_food_logs` ADD FOREIGN KEY (`recipe_id`) REFERENCES `recipes` (`id`);

ALTER TABLE `daily_habit_logs` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `supplements` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `daily_supplement_logs` ADD FOREIGN KEY (`daily_habit_log_id`) REFERENCES `daily_habit_logs` (`id`);

ALTER TABLE `daily_supplement_logs` ADD FOREIGN KEY (`supplement_id`) REFERENCES `supplements` (`id`);

ALTER TABLE `joint_discomfort_logs` ADD FOREIGN KEY (`daily_habit_log_id`) REFERENCES `daily_habit_logs` (`id`);

ALTER TABLE `supplements` ADD FOREIGN KEY (`user_id`) REFERENCES `workout_exercises` (`id`);
