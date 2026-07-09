-- V4: planned timeframe for epics, used by the roadmap view.

alter table epics add column start_date date;
alter table epics add column end_date date;
