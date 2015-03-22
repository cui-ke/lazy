drop table photo;
create table photo(id varchar2(12) primary key,
                     lib varchar2(32)not null,
                     refimg varchar2(32));


insert into photo values ('1','drapeau france','fr.gif');
insert into photo values ('2','joconde','joconde.jpg');

insert into photo values (photoid.nextval,'gobelet d''enfant','photo/DCP_0347.JPG');
insert into photo values (photoid.nextval,'bouteille d''eau','photo/DCP_0348.JPG');
insert into photo values (photoid.nextval,'citron','photo/DCP_0349.JPG');
insert into photo values (photoid.nextval,'fin de repas','photo/DCP_0350.JPG');
insert into photo values (photoid.nextval,'dessus de réfrigérateur','photo/DCP_0351.JPG');
insert into photo values (photoid.nextval,'évier','photo/DCP_0352.JPG');
insert into photo values (photoid.nextval,'cuisinière','photo/DCP_0353.JPG');
insert into photo values (photoid.nextval,'intérieure de réfrigérateur','photo/DCP_0354.JPG');
insert into photo values (photoid.nextval,'porte de réfrigérateur','photo/DCP_0355.JPG');
insert into photo values (photoid.nextval,'pause café','photo/DCP_0356.JPG');
insert into photo values (photoid.nextval,'bord de fenêtre','photo/DCP_0357.JPG');
insert into photo values (photoid.nextval,'rangement de cuisine','photo/DCP_0358.JPG');
insert into photo values (photoid.nextval,'poubelle','photo/DCP_0359.JPG');
insert into photo values (photoid.nextval,'linges cuisine','photo/DCP_0360.JPG');
insert into photo values (photoid.nextval,'intruments de cuisine','photo/DCP_0361.JPG');
insert into photo values (photoid.nextval,'mandarine','photo/DCP_0366.JPG');
insert into photo values (photoid.nextval,'fleur en pot','photo/DCP_0367.JPG');
insert into photo values (photoid.nextval,'biberon','photo/DCP_0368.JPG');
insert into photo values (photoid.nextval,'bavette','photo/DCP_0369.JPG');
insert into photo values (photoid.nextval,'chaise','photo/DCP_0370.JPG');
insert into photo values (photoid.nextval,'table','photo/DCP_0371.JPG');
insert into photo values (photoid.nextval,'brosse à vaisselle','photo/DCP_0372.JPG');
insert into photo values (photoid.nextval,'cuillière','photo/DCP_0373.JPG');
insert into photo values (photoid.nextval,'fourchette','photo/DCP_0374.JPG');
insert into photo values (photoid.nextval,'couteau','photo/DCP_0375.JPG');
insert into photo values (photoid.nextval,'services en bois','photo/DCP_0376.JPG');
insert into photo values (photoid.nextval,'verre','photo/DCP_0377.JPG');
insert into photo values (photoid.nextval,'cuillière','photo/DCP_0378.JPG');
insert into photo values (photoid.nextval,'tasse à café, mug','photo/DCP_0379.JPG');
insert into photo values (photoid.nextval,'assiette','photo/DCP_0380.JPG');
insert into photo values (photoid.nextval,'bol','photo/DCP_0381.JPG');
insert into photo values (photoid.nextval,'bol','photo/DCP_0382.JPG');
insert into photo values (photoid.nextval,'casserole','photo/DCP_0384.JPG');
insert into photo values (photoid.nextval,'huile d''olive','photo/DCP_0385.JPG');
insert into photo values (photoid.nextval,'tomates','photo/DCP_0386.JPG');
insert into photo values (photoid.nextval,'concombres','photo/DCP_0387.JPG');
insert into photo values (photoid.nextval,'bouteille de vin','photo/DCP_0388.JPG');
insert into photo values (photoid.nextval,'médicaments','photo/DCP_0389.JPG');
insert into photo values (photoid.nextval,'','photo/DCP_03.JPG');



commit;

drop table photodesc;
create table photodesc(id varchar2(12) ,
                     auteur varchar2(32)not null,
                     com varchar2(4000),
                     primary key (id,auteur));


insert into photodesc values ('1','JG','Le drapeau français est composé de trois couleurs: bleu, blanc et rouge');
insert into photodesc values ('1','MT','The french flag is white, red and blue');
insert into photodesc values ('2','JG','La Joconde est un tableau célèbre qui est visible au musée du Louvre');
insert into photodesc values ('2','MT','La Joconde a été peinte par Léonard de Vinci');

commit;

                     
create sequence photoid;