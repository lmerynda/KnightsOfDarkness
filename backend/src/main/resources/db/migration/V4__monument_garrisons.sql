-- soldiers stand guard with an ally or at their alliance's monument, never both
alter table kingdom_reinforcement_entity alter column host_name drop not null;
alter table kingdom_reinforcement_entity add column monument_alliance varchar(255);
alter table kingdom_reinforcement_entity add constraint kingdom_reinforcement_entity_monument_fk foreign key (monument_alliance) references monument_entity;
alter table kingdom_reinforcement_entity add constraint kingdom_reinforcement_entity_one_post check ((host_name is null) <> (monument_alliance is null));

-- every battle so far left all monuments standing
update battle_report_entity set report = report || '{"stagesRazed": 0}' where report is not null;
