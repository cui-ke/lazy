drop table projets;
create table projets(id varchar2(12) primary key,
                     lib varchar2(32)not null,
                     com varchar2(4000));


insert into projets values ('GRAPH','Génération de graphique SVG','Génération de graphique SVG, bla, bla');
insert into projets values ('DOCU','Gestion de la documentation','Gestion de la documentation, bla, bla');
commit;

drop table develops;
create table develops(id varchar2(12) primary key,
                     name varchar2(32)not null,
                     com varchar2(4000));


insert into develops values ('RZ','Richard Zbinden','junior');
insert into develops values ('NS','Nicolas Estanove','senior');
insert into develops values ('JG','Jacques Guyot','');
commit;

drop table projdev;
create table projdev(idproj varchar2(12) not null,
                     iddev varchar2(12)not null,
                     com varchar2(4000),
                     primary key (idproj,iddev));


insert into projdev values ('GRAPH','JG','Concepteur');
insert into projdev values ('GRAPH','RZ','Développeur');
insert into projdev values ('DOCU','NS','Concepteur - Développeur');
commit;
                     
 
