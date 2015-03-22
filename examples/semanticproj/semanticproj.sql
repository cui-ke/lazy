drop table obj;
create table obj(id varchar2(12) primary key,
                 lib varchar2(32)not null,
                 com varchar2(4000));


insert into obj values ('GRAPH','Génération de graphique SVG','Génération de graphique SVG, bla, bla');
insert into obj values ('JG','Jacques Guyot','');
insert into obj values ('GF','Gilles Favre','');
insert into obj values ('USER','catégorie des utilisateurs','');
insert into obj values ('PROJECT','catégorie des développeurs','');
commit;

drop table semantic;
create table semantic(id varchar2(12) primary key,
                      name varchar2(32)not null,
                      com varchar2(4000));


insert into semantic values ('ISA','X is a Y','EX: JG is a USER');
insert into semantic values ('DEVELOP','X develops Y','EX: JG develops GRAPH');
insert into semantic values ('USE','X uses Y','GF uses GRAPH');
commit;

drop table link;
create table link(idX varchar2(12) not null,
                     idSens varchar2(12)not null,
                     idY varchar2(12)not null,
                     com varchar2(4000),
                     primary key (idX,idSens,IdY));


insert into link values('JG','DEVELOP','GRAPH','');
insert into link values('GRAPH','ISA','PROJECT','');
insert into link values('JG','ISA','USER','');
insert into link values('GF','ISA','USER','');
insert into link values('GF','USE','GRAPH','');
commit;
                     
 
