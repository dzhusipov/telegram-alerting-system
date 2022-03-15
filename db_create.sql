create table chat_code
(
    chat_id,
    code,
    start_time  datetime,
    finish_time datetime
);

create table chat_emails
(
    chat_id,
    name,
    chat_name
);

create table chat_events
(
    chat_id,
    name,
    chat_name
);

create table chat_rights
(
    chat_id    int,
    authorized boolean
);

create table config
(
    name,
    value
);

