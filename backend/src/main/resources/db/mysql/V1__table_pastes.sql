
CREATE TABLE `pastes` (
     `id` varchar(255) NOT NULL,
     `version` BigInt NOT NULL,
     `remote_address` varchar(255) DEFAULT NULL,
     `date_created` datetime(6) NOT NULL,
     `date_of_expiry` datetime(6) DEFAULT NULL,
     `date_deleted` datetime(6) DEFAULT NULL,
     `title` varchar(512) DEFAULT NULL,
     `content` varchar(4096) NOT NULL,
     `is_encrypted` TINYINT NOT NULL,
     `exposure` ENUM('PUBLIC', 'UNLISTED', 'ONCE') NOT NULL,
     `views` BigInt not null,
     `last_viewed` datetime(6),
     PRIMARY KEY (`id`),
     FULLTEXT (`title`),
     FULLTEXT (`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `pastes_ttl` ON `pastes` (`date_deleted`, `date_of_expiry`);
