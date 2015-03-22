-- HSQL specific :
-- IDENTITY means primary key with autoincrement
--
create table exhibition(exno integer IDENTITY,title varchar(500),description varchar(500),isvirtual varchar(500),organizer varchar(500));
create table ex_content(work integer,exhibition integer, org_comment varchar(500));
create table work(wno integer IDENTITY, author integer,title varchar(500),support varchar(500),c_date varchar(500),height varchar(500),width varchar(500),acquired varchar(500),text varchar(500),audio varchar(500),picture varchar(500),movie varchar(500));
create table art_cnty(artist integer,country varchar(500),type varchar(500));
create table museum(mno integer,name varchar(500),city varchar(500),country varchar(500),url varchar(500));
create table ownership(work integer,museum integer);
-- IDENTITY means primary key with autoincrement. Specific to HSQL
create table artist(ano integer IDENTITY,name varchar(500),birthdate integer,deathdate integer);
--
-- Grant access to everybody to the tables
insert into lazy_rolenode values ('PUBLIC','exhibition','TABLE','NO','999','--');
insert into lazy_rolenode values ('PUBLIC','ex_content','TABLE','NO','999','nodef');
insert into lazy_rolenode values ('PUBLIC','work','TABLE','NO','999','nodef');
insert into lazy_rolenode values ('PUBLIC','art_cnty','TABLE','NO','999','nodef');
insert into lazy_rolenode values ('PUBLIC','artist','TABLE','NO','999','nodef');
