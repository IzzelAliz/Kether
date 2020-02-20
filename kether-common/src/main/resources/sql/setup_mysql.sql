create table if not exists example
(
    player_id varchar(36)                    not null,
    quest     varchar(64)                    not null,
    context   mediumtext,
    index (player_id, quest),
    index (quest)
) default charset = utf8mb4;