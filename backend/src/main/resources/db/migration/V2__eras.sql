-- rounds become eras: the clock only caps an era now, and the hall of fame records who won it and how it ended
alter table game_round_entity rename to game_era_entity;
alter table game_era_entity rename column length_in_ticks to max_length_in_ticks;
alter table game_era_entity add column winning_alliance varchar(255);
alter table game_era_entity add column ended_by varchar(255);

update game_era_entity set max_length_in_ticks = 2880 where ended_at is null;
update game_era_entity
   set ended_by = 'clock',
       winning_alliance = final_rankings -> 'alliances' -> 0 ->> 'allianceName'
 where ended_at is not null;

update notification_entity set kind = 'era' where kind = 'round';
