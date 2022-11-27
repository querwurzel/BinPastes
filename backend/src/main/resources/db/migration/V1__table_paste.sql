CREATE TABLE `paste` (
     `id` varchar(255) NOT NULL,
     `content` varchar(255) DEFAULT NULL,
     `date_created` datetime(6) DEFAULT NULL,
     `expiry` datetime(6) DEFAULT NULL,
     `remote_ip` varchar(255) DEFAULT NULL,
     `title` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
