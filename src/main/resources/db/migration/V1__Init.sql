SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

CREATE TABLE `site` (
  `id` int(1) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `site_name` varchar(45) NOT NULL,
  `site_url` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE `category` (
  `id` int(1) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `display_order` int(11) DEFAULT NULL,
  `description_ru` varchar(30) NOT NULL,
  `description_uk` varchar(30) NOT NULL,
  `description_en` varchar(30) NOT NULL,
  `thumb` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE `author` (
  `id` int(1) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `api_id` varchar(45) NOT NULL,
  `site_id` int(1) UNSIGNED NOT NULL,
  `category_id` int(1) UNSIGNED NOT NULL,
  `author_name` varchar(85) NOT NULL,
  `author_photo` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE `author`
  ADD KEY `siteID` (`site_id`),
  ADD KEY `categoryID` (`category_id`),
  ADD CONSTRAINT `site` FOREIGN KEY (`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE `post` (
  `id` varchar(20) NOT NULL PRIMARY KEY,
  `api_id` varchar(20) NOT NULL,
  `author_id` int(1) UNSIGNED NOT NULL,
  `text` text NOT NULL,
  `date` datetime NOT NULL,
  `url` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE `post`
  ADD UNIQUE KEY `UNIQUE` (`api_id`,`author_id`) USING BTREE,
  ADD KEY `AUTHOR` (`author_id`),
  ADD CONSTRAINT `AUTHOR` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE `att` (
  `id` int(1) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `post_id` varchar(20) NOT NULL,
  `att_i` int(1) UNSIGNED NOT NULL,
  `type` set('photo') NOT NULL,
  `att_api_id` varchar(30) DEFAULT NULL,
  `photo_height` int(1) UNSIGNED DEFAULT NULL,
  `photo_width` int(1) UNSIGNED DEFAULT NULL,
  `photo_130` text,
  `photo_604` text,
  `photo_807` text,
  `photo_1280` text,
  `photo_2560` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE `att`
  ADD UNIQUE KEY `UNIQUE` (`post_id`,`att_i`),
  ADD CONSTRAINT `post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

COMMIT;
