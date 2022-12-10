create table pastes (
    id varchar(255) not null,
    date_created timestamp not null,
    date_of_expiry timestamp,
    date_deleted timestamp,
    remote_ip varchar(255),
    title varchar(512),
    content varchar(4048) not null,
    primary key (id)
);

create index pastes_deleted on pastes (date_deleted);
create index pastes_expired on pastes (date_of_expiry);
