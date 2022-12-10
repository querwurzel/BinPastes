CREATE TABLE `pastes` (
     `id` varchar(255) NOT NULL,
     `date_created` datetime(6) NOT NULL,
     `title` varchar(512) DEFAULT NULL,
     `content` varchar(4048) NOT NULL,
     `remote_ip` varchar(255) DEFAULT NULL,
     `expiry` datetime(6) DEFAULT NULL,
     `is_deleted` TINYINT(1) NOT NULL,
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
