SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `user_groups`;
CREATE TABLE `user_groups`
(
    `id`              char(32)                                                      NOT NULL,
    `admin_id`        char(32)                                                      NOT NULL,
    `name`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL,
    `creator`         char(32)                                                      NOT NULL,
    `description`     varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `ext`             text                                                          NULL,
    `alive`           bool                                                          NULL DEFAULT TRUE,
    `create_time`     datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `unique_user_group_name` (`name`, `alive`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `user_group_maps`;
CREATE TABLE `user_group_maps`
(
    `group_id`        char(32)                                                      NOT NULL,
    `user_id`         char(32)                                                      NOT NULL,
    `create_time`     datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`group_id`, `user_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    `id`              char(32)                                                      NOT NULL,
    `name`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL,
    `role`            varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `last_login_time` datetime                                                      NULL,
    `alive`           bool                                                          NULL DEFAULT TRUE,
    `create_time`     datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `unique_user_name` (`name`, `alive`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;


DROP TABLE IF EXISTS blocked_words;
CREATE TABLE blocked_words
(
    `id`          char(32)                                                     NOT NULL,
    `value`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `enabled`     bool                                                         NOT NULL DEFAULT TRUE,
    `creator`     char(32)                                                     NOT NULL,
    `create_time` datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `unique_value` (`value`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `assistants`;
CREATE TABLE `assistants`
(
    `id`            char(32)                                                      NOT NULL,
    `name`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL,
    `permit`        int                                                           NOT NULL,
    `llm_model`     varchar(50)                                                   NOT NULL,
    `description`   varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `system_prompt` varchar(50)                                                   NOT NULL,
    `ext`           text                                                          NULL,
    `creator`       char(32)                                                      NULL,
    `alive`           bool                                                        NULL DEFAULT TRUE,
    `create_time`   datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `unique_assistant_name` (`name`, `alive`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations`
(
    `id`            char(32)                                                      NOT NULL,
    `assistant_id`  char(32)                                                      NOT NULL,
    `name`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL,
    `creator`       char(32)                                                      NULL,
    `create_time`   datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`
(
    `id`            char(32)                                                      NOT NULL,
    `conversation_id` char(32)                                                    NOT NULL,
    `query`         text                                                          NULL,
    `answer`        text                                                          NULL,
    `context`       longtext                                                      NULL,
    `create_time`   datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `assistant_bases`;
DROP TABLE IF EXISTS `assistant_bases`;
CREATE TABLE assistant_bases (
     `assistant_id`   char(32)            NOT NULL,
     `base_id`        char(32)            NOT NULL,
     PRIMARY KEY (assistant_id, base_id)
);

DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base`
(
    `id`          char(32)                                                      NOT NULL,
    `name`        varchar(50)  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `permit`      int                                                           NOT NULL,
    `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `embed_model` varchar(50)                                                   NOT NULL,
    `ext`         text                                                          NULL,
    `creator`     char(32)                                                      NULL,
    `alive`           bool                                                      NULL DEFAULT TRUE,
    `create_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `unique_knowledge_base_name` (`name`, `alive`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `knowledge_base_tags`;
CREATE TABLE knowledge_base_tags (
    `base_id`   char(32)            NOT NULL,
    `tag`     VARCHAR(50)           NOT NULL,
     PRIMARY KEY (base_id, `tag`)
);

DROP TABLE IF EXISTS `documents`;
CREATE TABLE `documents`
(
    `id`             char(32)                                                      NOT NULL,
    `bucket`         varchar(50)  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `key`            varchar(50)  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `raw_key`        varchar(50)  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `cover_key`      varchar(50)  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `file_name`      varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `title`          varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `base_id`        char(32)                                                          NULL,
    `creator`        char(32)                                                      NOT NULL,
    `blocked_reason` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `alive`           bool                                                         NULL DEFAULT TRUE,
    `create_time`    datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `s3_unique_key` (`bucket`, `key`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
