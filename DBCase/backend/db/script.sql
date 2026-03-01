drop table if exists users;

create table users (
    id serial primary key,
    username varchar(50),
    password varchar(255),
    chart json
);

insert into users values (1, 'admin', '$2y$10$X8DirCUuFYgbou/JKv4j0ORkOWdJeAt8c7nCzbyfRVaWG1gZCOpcG',null);
insert into users values (2, 'user', '$2y$10$javS4da769B/wYSpuzHEFeEe2X/0pKS5GGFcM8Tjz.e1FfBXCQog6', null);

-- Synchronize the sequence so new inserts don't collide with manual IDs
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));