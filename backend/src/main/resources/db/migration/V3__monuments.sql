-- an alliance's monument, gone with the alliance or the era
create table monument_entity (
    alliance_name varchar(255) not null,
    seat varchar(255) not null,
    stages_built double precision not null,
    gold bigint not null,
    iron bigint not null,
    tools bigint not null,
    food bigint not null,
    announced_stage integer not null,
    upkeep_unpaid boolean not null,
    primary key (alliance_name)
);

alter table monument_entity add constraint monument_entity_alliance_fk foreign key (alliance_name) references alliance_entity;
