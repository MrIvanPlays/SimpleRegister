CREATE TABLE IF NOT EXISTS simpleregister_passwords(
  `id` INT AUTO_INCREMENT,
  `name` VARCHAR(255),
  `uuid` VARCHAR(36),
  `ip` VARCHAR(255),
  `password` VARCHAR(255)
  PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4;
CREATE INDEX `simpleregister_passwords_uuid` ON `simpleregister_passwords` (`uuid`);