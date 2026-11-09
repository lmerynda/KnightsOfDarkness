-- a complete monument wins the era once it has stood, fed, for monument.holdTicks ticks
alter table monument_entity add column held_ticks integer not null default 0;
