
CREATE TABLE `pastes` (
     `id` varchar(255) NOT NULL,
     `remote_address` varchar(255) DEFAULT NULL,
     `date_created` datetime(6) NOT NULL,
     `date_of_expiry` datetime(6) DEFAULT NULL,
     `date_deleted` datetime(6) DEFAULT NULL,
     `title` varchar(512) DEFAULT NULL,
     `content` varchar(4048) NOT NULL,
     `is_encrypted` TINYINT NOT NULL,
     PRIMARY KEY (`id`),
     FULLTEXT (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `legit_pastes` ON `pastes` (`date_deleted`, `date_of_expiry`);
