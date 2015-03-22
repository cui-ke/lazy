-- Lazy System Administration
--
-- Initial Data
--
--
-- Users
--
delete from lazy_users;
insert into lazy_users values ('ADMIN','Administrator','Lazy administrator','x','ADMIN','SYSTEM','EN/UK','TABLE');
insert into lazy_users values ('PUBLIC','Anybody','Visitor','x','NO','PUBLIC','EN/UK','TABLE');
insert into lazy_users values ('DEVEL','Developer','Node developer','x','NO','PUBLIC','EN/UK','TABLE');
insert into lazy_users values ('USER1','User one','Sample user one','user1','NO','PUBLIC','EN/UK','TABLE');
insert into lazy_users values ('USER2','User two','Sample user one','user2','NO','PUBLIC','EN/UK','TABLE');
--
-- Data Groups
--
delete from lazy_data;
-- insert into lazy_data values ('SYSTEM','Server management');
insert into lazy_data values ('PUBLIC','No specific access, represents any data group');
insert into lazy_data values ('*','Represents any data group');
insert into lazy_data values ('EXAMPLE1','Sample Data Set');
--
-- Roles
--
delete from lazy_roles;
-- insert into lazy_roles values ('SYSTEM_ADMIN','System administration');
insert into lazy_roles values ('DEV','Node developer');
insert into lazy_roles values ('PUBLIC','Granted to every user');
--
-- Grant roles to users
--
--  lazy_grants(user, data group, role)
delete from lazy_grants;
    -- User PUBLIC has role PUBLIC in all dataspaces
insert into lazy_grants values('PUBLIC','*', 'PUBLIC');
    -- DEVEL has roles DEV and PUBLIC everywhere
insert into lazy_grants values('DEVEL','*', 'DEV'); 
insert into lazy_grants values('DEVEL','*', 'PUBLIC');
    -- USER1 and USER2 have role PUBLIC everywhere
insert into lazy_grants values('USER1','*', 'PUBLIC');
insert into lazy_grants values('USER2','*', 'PUBLIC');
--
--
-- Grant node access to roles
--
delete from lazy_RoleNode;
-- insert into lazy_RoleNode values ('SYSTEM_ADMIN', 'NODE.*', 'LAZY', 'NO',999, '');
-- insert into lazy_RoleNode values ('SYSTEM_ADMIN', 'ADMIN.*', 'LAZY', 'NO',999, '');
--
-- a developer may access all nodes
insert into lazy_RoleNode values ('DEV', 'ICON.*', 'LAZY', 'NO',999, '');
insert into lazy_RoleNode values ('DEV', 'NODE.*', 'LAZY', 'NO',999, '');
insert into lazy_RoleNode values ('DEV', 'ADMIN.*', 'LAZY', 'NO',999, '');
-- ... and modify nodes
insert into lazy_RoleNode values ('DEV', 'LAZY_NODES', 'TABLE', 'NO',999, '');
insert into lazy_RoleNode values ('DEV', 'LAZY_ACTIONS', 'TABLE', 'NO',999, '');
--
--  The PUBLIC role may access several nodes
insert into lazy_RoleNode values ('PUBLIC','ICON.*','LAZY','NO','999','nodef');
insert into lazy_RoleNode values ('PUBLIC','ADMIN.all','LAZY','NO','999','nodef');
-- insert into lazy_RoleNode values ('PUBLIC','ADMIN.menuitem','LAZY','NO','999','nodef');
insert into lazy_RoleNode values ('PUBLIC','ADMIN.gprid_askmodify','LAZY','NO','999','nodef');
insert into lazy_RoleNode values ('PUBLIC','ADMIN.grpid_modify','LAZY','NO','999','nodef');
insert into lazy_RoleNode values ('PUBLIC','ADMIN.list_usergrpid','LAZY','NO','999','nodef');
insert into lazy_RoleNode values ('PUBLIC','ADMIN.maj_pwd_users','LAZY','NO','999','nodef');
insert into lazy_RoleNode values ('PUBLIC','ADMIN.PWDMODIF','TABLE','NO','999','(VUE) pour la mise à jour du mot de passe');
--
-- Projects
--
delete from lazy_projects;
insert into lazy_projects values ('ADMIN','Users, data, and project management', 'html', 
'DICTLAZY', 'xsl/lazy.xsl', 'css/lazy.css', 'bckgnd/neutral.jpg');
insert into lazy_projects values ('ICON','A set of userful icons', 
'html', 'DICTLAZY', 'xsl/lazy.xsl', 'css/lazy.css', 'bckgnd/neutral.jpg');
insert into lazy_projects values ('NODE','Node management 
(development)', 'html', 'DICTLAZY', 'xsl/lazy.xsl', 'css/lazy.css', 
'bckgnd/neutral.jpg');
--
-- Sample projects
--
insert into lazy_projects values ('MW','A mini virtual museum', 'html', 
'DICTLAZY', 'xsl/lazy.xsl', 'museum/museum.css', 'bckgnd/neutral.jpg');
-- grant access to role PUBLIC
insert into lazy_RoleNode values ('PUBLIC','MW.*','LAZY','NO','999','nodef');
 -- PUBLIC may update all the tables
 insert into lazy_RoleNode values ('PUBLIC','artist','TABLE','NO','999','nodef');
 insert into lazy_RoleNode values ('PUBLIC','exhibition','TABLE','NO','999','nodef');
 insert into lazy_RoleNode values ('PUBLIC','ex_content','TABLE','NO','999','nodef');
 insert into lazy_RoleNode values ('PUBLIC','work','TABLE','NO','999','nodef');
 insert into lazy_RoleNode values ('PUBLIC','art_cnty','TABLE','NO','999','nodef');
 insert into lazy_RoleNode values ('PUBLIC','museum','TABLE','NO','999','nodef');
 insert into lazy_RoleNode values ('PUBLIC','ownership','TABLE','NO','999','nodef');
--
insert into lazy_projects values ('MYSITE','A demo project', 'html', 
'DICTLAZY', 'xsl/lazy.xsl', 'css/lazy.css', 'bckgnd/neutral.jpg');
-- grant access to role DEV
insert into lazy_RoleNode values ('DEV','MYSITE.*','LAZY','NO','999','nodef');
--
-- Connections
--
delete from lazy_connects;
--
insert into lazy_connects values ('Oracle1','Sample connection to an Oralce DB','oracle.jdbc.driver.OracleDriver',
                                 'jdbc:oracle:thin:@hostname:1521:dbname','user','pwd');
insert into lazy_connects values ('Hsqldb','Sample connection to an Hsql DB','org.hsqldb.jdbcDriver',
                                 'jdbc:hsqldb:hsql://localhost','sa','');
								 
--
--
-- Strings (Codes)
--
delete from lazy_codes;
--
-- (CATID, CODEID, ABR, LISTDEF)
insert into lazy_codes values('NODETYPE','LAZY','LAZY','Y');
insert into lazy_codes values('NODETYPE','TABLE','TABLE','Y');
--
insert into lazy_codes values('DISPLAYTYPE','ON MENU','ON MENU','Y');
--
insert into lazy_codes values('ADMI','ADMIN','ADMIN','Y');
insert into lazy_codes values('ADMI','NO','NO','Y');
--
insert into lazy_codes values('LANG','fr','French','Y');
insert into lazy_codes values('LANG','en','English','Y');
insert into lazy_codes values('LANG','es','Spanish','Y');
insert into lazy_codes values('LANG','eo','Esperanto','Y');
insert into lazy_codes values('LANG','de','German','Y');
insert into lazy_codes values('LANG','it','Italian','Y');
insert into lazy_codes values('LANG','rm','Raeto-Romance','Y');
Raeto-Romance
--
commit;


