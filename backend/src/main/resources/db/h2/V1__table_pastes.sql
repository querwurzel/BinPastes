create table pastes (
    id varchar(255) not null,
    date_created timestamp not null,
    expiry timestamp,
    remote_ip varchar(255),
    title varchar(512),
    content varchar(4048) not null,
    is_deleted boolean not null,
    primary key (id)
);

create index pastes_deleted on pastes (is_deleted);
create index pastes_expired on pastes (expiry);
