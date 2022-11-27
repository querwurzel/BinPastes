create table paste (
    id varchar(255),
    date_created timestamp,
    expiry timestamp,
    remote_ip varchar(255),
    title varchar(255),
    content varchar(255),
    primary key (id)
);
