create keyspace stackoverflow with replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

create table stackoverflow.error
(
    id   bigint,
    name varchar,
    primary key (id)
);
