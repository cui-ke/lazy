rem adaptation du schéma termino ....

drop table Term_ConceptDef;
create table Term_ConceptDef(
  grpid varchar2(12),
  conceptid number primary key , 
  def varchar2(4000), 
  context varchar2(4000),  
  notes varchar2(4000),
  recsource varchar2(4000),
  origin varchar2(4000),
  status varchar2(12),
  authorid varchar2(12),
  authormodif date,
  terminologid varchar2(12),
  terminologmodif date
);

insert into term_conceptdef
 select 'TERM1',isn,def,context,notes,recsource,origin,
        'VALID','GUYOT',sysdate,'GUYOT',sysdate from isn;
 
select count(*) from  term_conceptdef;


drop table Term_Terms;
create table Term_Terms(
  grpid varchar2(12),
  termid number primary key,
  ranking number,
  conceptid number, 
  term varchar2(4000), 
  abbr varchar2(40),
  source varchar2(4000),
  usage varchar2(4000),
  origin varchar2(4000),
  lang varchar2(12),
  status varchar2(12),
  authorid varchar2(12),
  authormodif date,
  terminologid varchar2(12),
  terminologmodif date,
  fiability varchar2(12) 
);

insert into Term_Terms
 select 'TERM1',term_id,0,isn,term,abbr,source,usage,origin,
         decode (lang_no,1,'ENG',2,'FRE',3,'SPA',4,'GER','???'),
        'VALID','GUYOT',sysdate,'GUYOT',sysdate,'GOOD' 
      from terms;
 
create index Terms_i1 on Term_Terms(conceptid,lang);

create index Terms_i2 on Term_terms(grpid,abbr);

select max(termid) from term_terms; 
 
create sequence term_seqid start with 100000;
 
select count(*) from  Term_Terms;

commit;

rem un meme terme pour plusieurs concept!

select term,count(*) from Term_Terms group by term having count(*)>1;

select * from Term_Terms where conceptid=15864;

rem delete from term_conceptdef where conceptid>=100000;
rem delete from term_terms where termid>=100000;


