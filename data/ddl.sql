CREATE TABLE `USERS` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(150) NOT NULL,
  `email` VARCHAR(254) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_users_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ORGANIZE` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `path` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `owner_id` BIGINT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_organize_path` (`path`),
  CONSTRAINT `FK_organize_owner` FOREIGN KEY (`owner_id`) REFERENCES `USERS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ORGANIZE_MEMBER` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `organize_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `role` VARCHAR(32) NOT NULL,
  `joined_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_organize_member_user` (`organize_id`,`user_id`),
  CONSTRAINT `FK_organize_member_organize` FOREIGN KEY (`organize_id`) REFERENCES `ORGANIZE` (`id`),
  CONSTRAINT `FK_organize_member_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `REPOSITORY` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `organize_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `path` VARCHAR(255) NOT NULL,
  `repository_type` VARCHAR(32) NOT NULL DEFAULT 'GIT',
  `owner_id` BIGINT NULL,
  `credential_id` VARCHAR(128) NULL,
  `clone_path` VARCHAR(512) NULL,
  `description` TEXT,
  `default_branch` VARCHAR(255) NOT NULL,
  `visibility` VARCHAR(32) NOT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'REGISTERED',
  `last_synced_at` TIMESTAMP NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_repository_path` (`path`),
  CONSTRAINT `FK_repository_organize` FOREIGN KEY (`organize_id`) REFERENCES `ORGANIZE` (`id`),
  CONSTRAINT `FK_repository_owner` FOREIGN KEY (`owner_id`) REFERENCES `USERS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `REPOSITORY_MEMBER` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `repository_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `role` VARCHAR(32) NOT NULL,
  `added_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_repository_member_user` (`repository_id`,`user_id`),
  CONSTRAINT `FK_repository_member_repository` FOREIGN KEY (`repository_id`) REFERENCES `REPOSITORY` (`id`),
  CONSTRAINT `FK_repository_member_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `BRANCH` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `repository_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `is_locked` BOOLEAN NOT NULL DEFAULT FALSE,
  `locked_by` BIGINT,
  `locked_at` TIMESTAMP NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_branch_repository_name` (`repository_id`,`name`),
  CONSTRAINT `FK_branch_repository` FOREIGN KEY (`repository_id`) REFERENCES `REPOSITORY` (`id`),
  CONSTRAINT `FK_branch_locked_by` FOREIGN KEY (`locked_by`) REFERENCES `USERS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `RUNNER` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `token` VARCHAR(255) NOT NULL,
  `description` VARCHAR(512),
  `status` VARCHAR(32) NOT NULL,
  `ip_address` VARCHAR(45),
  `last_heartbeat_at` TIMESTAMP NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `RUNNER_ASSIGNMENT` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `runner_id` BIGINT NOT NULL,
  `target_type` VARCHAR(32) NOT NULL,
  `target_id` BIGINT,
  `assigned_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_runner_assignment_runner` FOREIGN KEY (`runner_id`) REFERENCES `RUNNER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `JOB` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `repository_id` BIGINT NOT NULL,
  `commit_hash` VARCHAR(64) NOT NULL,
  `branch_name` VARCHAR(255) NOT NULL,
  `triggered_by` BIGINT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_job_repository` FOREIGN KEY (`repository_id`) REFERENCES `REPOSITORY` (`id`),
  CONSTRAINT `FK_job_triggered_by_user` FOREIGN KEY (`triggered_by`) REFERENCES `USERS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `JOB_HISTORY` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `runner_id` BIGINT,
  `status` VARCHAR(32) NOT NULL,
  `log_path` VARCHAR(1024),
  `started_at` TIMESTAMP NULL,
  `finished_at` TIMESTAMP NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_job_history_job` FOREIGN KEY (`job_id`) REFERENCES `JOB` (`id`),
  CONSTRAINT `FK_job_history_runner` FOREIGN KEY (`runner_id`) REFERENCES `RUNNER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
