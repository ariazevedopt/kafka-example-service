create sequence IF NOT EXISTS example.user_1;

create table if not exists example.user_example
(
    id int not null primary key default nextval('example.user_1'::regclass),
    user_name varchar(255),
    age int,
    gender varchar(15),
    group_name varchar(255)
);