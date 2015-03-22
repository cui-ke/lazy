
-- drop table lazy_users;
-- drop table lazy_data;
-- drop table lazy_roles;
-- drop table lazy_RoleNode;
-- drop table lazy_grants;
-- drop table lazy_projects;
-- drop table lazy_connects;
-- drop table lazy_categories;
-- drop table lazy_codes;
-- drop table lazy_txt;
-- drop table lazy_txtlang;

create table lazy_users(
	UserId varchar2(12) primary key,
	InfoUser varchar2(2000)not null,
	com varchar2(2000),
	pwd varchar2(2000),
	admin varchar2(12), -- ADMIN => super user
	defaultGrpId varchar(12) not null, -- default Data space
	lang varchar(12),
	style varchar(12)
);



create table lazy_data(
	GrpId varchar(12) primary key,
	com varchar2(2000)
);

                     
create table lazy_roles(
	roleId varchar(12) primary key,
	com varchar2(2000)
);


create table lazy_RoleNode(
	roleId varchar(12) not null,
	NodeId varchar2(100)not null, -- either PROJECT.NODE or PROJECT.*
	TypeId varchar2(12)not null,  -- LAZY => instantiate a node, TABLE => update database table
	Display varchar2(12)not null, -- ON MENU => display this node in the main menu
	Seq number , -- display order
	lib varchar2(100), -- text to display
	primary key( roleid,nodeid,typeid)
);
 
create table lazy_grants (
	userid varchar(12) not null,
	grpid varchar2(12)not null, -- use '*' to grand this role to this user in all dataspaces 
	roleid varchar2(12)not null,
	primary key(userid,grpid,roleid));
                           

create table lazy_projects(
	projectId varchar(59) primary key,
	com varchar2(2000),
	nodetype varchar(20), -- xml, html, purexml, purehtml
	dbconnection varchar(100),
	xslurl varchar2(200), 
	cssurl varchar2(200), 
	bkgndurl varchar2(2000)
);

create table lazy_connects(
	connectId varchar2(12) primary key,
	com varchar2(2000),
	driver varchar2(100)not null,
	url varchar(100)not null,
	userdb varchar(100)not null,
	pwddb varchar2(100)not null
);
 

-- drop view lazy_usergrpid;
-- create view lazy_usergrpid as
--    select distinct userid,grpid 
--       from lazy_menu;
-- 	  
-- drop view lazy_grantnodes;
-- create view lazy_grantnodes as
--    select distinct g.userid,g.grpid,n.nodeid,n.typeid 
--       from lazy_grants g, lazy_rolenode n
--       where g.roleid=n.roleid;
-- 

-- String management and internationalization


create table lazy_categories(CatId varchar(12) primary key,
                          com varchar2(2000)not null
);

create table lazy_codes (catId varchar(12) not null,
                           codeid varchar2(12)not null,
                           abr    varchar2(40)not null,
                           listdef char(1)not null,
                           primary key(catid,codeid)
);

-- drop view lazy_defcode;
-- create view lazy_defcode as
--   select catid,codeid,abr from lazy_codes where listdef='Y';


create table lazy_txt(projectId varchar(12),
                        txtId varchar(24),
                        lib varchar2(4000)not null,
                        primary key(projectId,txtId)
);


create table lazy_txtlang(projectId varchar(12),
                        lang varchar(12),
                        txtId varchar(24),
                        lib varchar2(4000)not null,
                        primary key(projectId,lang,txtId)
);

-- drop view lazy_pwdmodif;
-- create view lazy_pwdmodif as
--    select userid, pwd from lazy_users
--    with check option;


-- drop view lazy_alltxt;
-- create view lazy_alltxt as
--    select projectid,lang, txtid,lib from lazy_txtlang
--    union
--    select projectid, 'FRE', txtid,lib from lazy_txt;
-- 
-- drop view lazy_menu;
-- create view lazy_menu as
--    select distinct g.userid,g.grpid,g.roleid,r.com 
--       from lazy_grants g, lazy_roles r, lazy_rolenode n
--       where g.roleid=r.roleid
--       and g.roleid=n.roleid and display='ON MENU';
-- 
-- 
-- drop view lazy_usergrpid;
-- create view lazy_usergrpid as
--    select distinct userid,grpid 
--       from lazy_menu;
-- 
-- 
-- drop view lazy_menuitem;
-- create view lazy_menuitem as
--    select g.userid,g.grpid,g.roleid,n.nodeid,n.seq,n.lib 
--       from lazy_grants g, lazy_rolenode n
--       where g.roleid=n.roleid and display='ON MENU';
-- 
-- 


commit;


