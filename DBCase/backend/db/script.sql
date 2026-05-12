drop table if exists user_custom_domains;
drop table if exists users;

create table users (
    id serial primary key,
    username varchar(50),
    password varchar(255),
    chart json,
    picture_url text,
    language varchar(20),
    theme varchar(20),
    expires_at timestamp
);

create table user_custom_domains (
    user_id bigint not null references users(id) on delete cascade,
    name varchar(100) not null,
    base varchar(100)
);

insert into users (id, username, password) values (1, 'admin', '$2y$10$X8DirCUuFYgbou/JKv4j0ORkOWdJeAt8c7nCzbyfRVaWG1gZCOpcG');
insert into users (id, username, password) values (2, 'user', '$2y$10$javS4da769B/wYSpuzHEFeEe2X/0pKS5GGFcM8Tjz.e1FfBXCQog6');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
