-- Schema for hsqldb
-- drop table nodes;
create table lazy_nodes(
   projectid varchar(100),
   name varchar(100),
   nbparam numeric, 
   pre varchar(4000), 
   items varchar(4000), 
   post varchar(4000), 
   collection varchar(4000), 
   selector varchar(4000), 
   ordering varchar(4000),
   groupby varchar(4000),
   plaintxt varchar(4000), 
   status varchar(12),
   error varchar(4000),
   cachesize numeric,
   primary key (projectid,name));
--
-- the database actions associated with each node ("on open")
--
create table lazy_actions(
   projectid varchar(100),
   nodename varchar(100),
   operation varchar(1000),
   seqno numeric
)
--
-- We also need a table with just one row
--
create table dual(dummy char(1));
insert into dual values('X');
--
-- And a table with "all" the natural numbers (including 0)
--
create table lazy_naturals(val integer);
insert into lazy_naturals values(0);
insert into lazy_naturals values(1);
insert into lazy_naturals values(2);
insert into lazy_naturals values(3);
insert into lazy_naturals values(4);
insert into lazy_naturals values(5);
insert into lazy_naturals values(6);
insert into lazy_naturals values(7);
insert into lazy_naturals values(8);
insert into lazy_naturals values(9);
--
-- table to store the dependencies between nodes and database objects (tables or views)
--
create table lazy_node_db_dep(
   PROJECTID varchar(100), 
   NODENAME varchar(100), 
   DBOBJECT varchar(100));

