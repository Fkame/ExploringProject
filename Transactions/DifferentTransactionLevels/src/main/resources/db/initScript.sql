create schema testing;

SET SCHEMA 'testing';

create table test_table
(
    id        bigint PRIMARY KEY,
    textField TEXT
);