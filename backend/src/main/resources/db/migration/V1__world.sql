-- The world as the entities described it when the schema moved to migrations. Enum columns are plain text, with no
-- check listing their values: the Java enums are the one list, so a new value needs no migration.

create table alliance_entity (
    emperor varchar(255) not null,
    name varchar(255) not null,
    primary key (name)
);

create table alliance_invitation_entity (
    invited_at timestamp(6) with time zone,
    id uuid not null,
    alliance varchar(255) not null,
    kingdom varchar(255) not null,
    primary key (id)
);

create table battle_report_entity (
    date timestamp(6) with time zone,
    id uuid not null,
    attacker varchar(255),
    defender varchar(255),
    report jsonb,
    primary key (id)
);

create table bot_entity (
    id uuid not null,
    kingdom_name varchar(255) unique,
    primary key (id)
);

create table game_round_entity (
    length_in_ticks integer not null,
    number integer not null,
    ticks_elapsed integer not null,
    ended_at timestamp(6) with time zone,
    started_at timestamp(6) with time zone,
    final_rankings jsonb,
    primary key (number)
);

create table kingdom_carriers_on_the_move_entity (
    carriers_count integer not null,
    resource_count integer not null,
    turns_left integer not null,
    id uuid not null,
    kingdom_name varchar(255) not null,
    resource varchar(255),
    target_kingdom_name varchar(255),
    primary key (id)
);

create table kingdom_entity (
    available_blacksmith integer,
    available_bowman integer,
    available_builder integer,
    available_carrier integer,
    available_cavalry integer,
    available_farmer integer,
    available_gold_miner integer,
    available_guard integer,
    available_infantry integer,
    available_iron_miner integer,
    available_spy integer,
    barracks integer,
    building_points integer,
    castle integer,
    farm integer,
    food integer,
    gold integer,
    gold_mine integer,
    guard_house integer,
    house integer,
    iron integer,
    iron_mine integer,
    land integer,
    market integer,
    mobile_blacksmith integer,
    mobile_bowman integer,
    mobile_builder integer,
    mobile_carrier integer,
    mobile_cavalry integer,
    mobile_farmer integer,
    mobile_gold_miner integer,
    mobile_guard integer,
    mobile_infantry integer,
    mobile_iron_miner integer,
    mobile_spy integer,
    protection_ticks integer not null,
    spy_guild integer,
    tools integer,
    tower integer,
    turns integer,
    unemployed integer,
    weapons integer,
    workshop integer,
    version bigint not null,
    alliance_name varchar(255),
    name varchar(255) not null,
    last_turn_report jsonb,
    primary key (name)
);

create table kingdom_ongoing_attack_entity (
    turns_at_border integer not null,
    turns_left integer not null,
    id uuid not null,
    joins_attack_id uuid,
    attack_type varchar(255),
    joins_kingdom_name varchar(255),
    kingdom_name varchar(255) not null,
    target_kingdom_name varchar(255),
    units jsonb,
    primary key (id)
);

create table kingdom_reinforcement_entity (
    turns_left integer not null,
    id uuid not null,
    host_name varchar(255) not null,
    kingdom_name varchar(255) not null,
    units jsonb,
    primary key (id)
);

create table kingdom_special_building_entity (
    building_points_put integer not null,
    building_points_required integer not null,
    is_max_level boolean not null,
    level integer not null,
    id uuid not null,
    building_type varchar(255),
    kingdom_name varchar(255) not null,
    primary key (id)
);

create table market_offer_entity (
    count integer not null,
    price integer not null,
    id uuid not null,
    kingdom_name varchar(255) not null,
    resource varchar(255),
    primary key (id)
);

create table market_transaction_entity (
    count integer not null,
    price integer not null,
    date timestamp(6) with time zone,
    id uuid not null,
    buyer varchar(255),
    resource varchar(255),
    seller varchar(255),
    primary key (id)
);

create table market_transaction_time_range_averages_entity (
    average_price integer not null,
    volume integer not null,
    from_date timestamp(6) with time zone,
    to_date timestamp(6) with time zone,
    id uuid not null,
    resource varchar(255),
    primary key (id)
);

create table message_entity (
    is_read boolean not null,
    to_alliance boolean not null,
    date timestamp(6) with time zone,
    id uuid not null,
    body varchar(2000),
    recipient varchar(255),
    sender varchar(255),
    primary key (id)
);

create table notification_entity (
    is_read boolean not null,
    date timestamp(6) with time zone,
    id uuid not null,
    kind varchar(255),
    kingdom_name varchar(255),
    message varchar(255),
    primary key (id)
);

create table spy_report_entity (
    date timestamp(6) with time zone,
    id uuid not null,
    kingdom_name varchar(255),
    report jsonb,
    primary key (id)
);

create table user_entity (
    email varchar(255) not null,
    kingdom_name varchar(255),
    password varchar(255),
    primary key (email)
);

alter table alliance_invitation_entity add constraint alliance_invitation_entity_alliance_fk foreign key (alliance) references alliance_entity;

alter table alliance_invitation_entity add constraint alliance_invitation_entity_kingdom_fk foreign key (kingdom) references kingdom_entity;

alter table bot_entity add constraint bot_entity_kingdom_name_fk foreign key (kingdom_name) references kingdom_entity;

alter table kingdom_carriers_on_the_move_entity add constraint kingdom_carriers_on_the_move_entity_kingdom_name_fk foreign key (kingdom_name) references kingdom_entity;

alter table kingdom_entity add constraint kingdom_entity_alliance_name_fk foreign key (alliance_name) references alliance_entity;

alter table kingdom_ongoing_attack_entity add constraint kingdom_ongoing_attack_entity_kingdom_name_fk foreign key (kingdom_name) references kingdom_entity;

alter table kingdom_reinforcement_entity add constraint kingdom_reinforcement_entity_host_name_fk foreign key (host_name) references kingdom_entity;

alter table kingdom_reinforcement_entity add constraint kingdom_reinforcement_entity_kingdom_name_fk foreign key (kingdom_name) references kingdom_entity;

alter table kingdom_special_building_entity add constraint kingdom_special_building_entity_kingdom_name_fk foreign key (kingdom_name) references kingdom_entity;

alter table market_offer_entity add constraint market_offer_entity_kingdom_name_fk foreign key (kingdom_name) references kingdom_entity;
