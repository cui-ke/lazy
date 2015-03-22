
delete from lazy_nodes where name='top';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'top',
 0,
''''', ''<a href="ns?a=ADMIN.all'', ''">'', ''Admin'', ''</a>'', '' ||| '', ''<a href="ns?a=ADMIN.all_projects'', ''">'', ''Projects'', ''</a>'', '' | '', ''<a href="ns?a=ADMIN.all_users'', ''">'', ''Users'', ''</a>'', '' | '', ''<a href="ns?a=ADMIN.all_data'', ''">'', ''Data Groups'', ''</a>'', '' | '', ''<a href="ns?a=ADMIN.all_roles'', ''">'', ''Roles'', ''</a>'', '' | '', ''<a href="ns?a=ADMIN.all_connects'', ''">'', ''DB Connections'', ''</a>'', ''<hr'', ''>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='all';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'all',
 0,
''''', ''<h2'', ''>'', ''Lazy administration by <font color="red">[[USER]]</font> in Data group [[GRP]]'', ''</h2>'', ''<blockquote'', ''>'', ''<h3'', ''>'', ''Objects'', ''</h3>'', ''<table'', '' width="'', ''70%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''cell_bg3'', ''"'', '' width="'', ''50%'', ''"'', ''>'', ''<a href="ns?a=ADMIN.all_projects'', ''">'', ''Projects'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''cell_bg3'', ''"'', ''>'', ''<a href="ns?a=ADMIN.all_users'', ''">'', ''Users'', ''</a>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''cell_bg3'', ''"'', ''>'', ''<a href="ns?a=ADMIN.all_connects'', ''">'', ''Database connections'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''cell_bg3'', ''"'', ''>'', ''<a href="ns?a=ADMIN.all_data'', ''">'', ''Data Groups'', ''</a>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''cell_bg3'', ''"'', ''>'', ''<a href="ns?a=ADMIN.all_roles'', ''">'', ''Roles'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''cell_bg3'', ''"'', ''>'', ''<a href="ns?a=ADMIN.all_categories'', ''">'', ''Codes'', ''</a>'', ''</td>'', ''</tr>'', ''</table>'', ''<h3'', ''>'', ''Operations'', ''</h3>'', ''<table'', '' width="'', ''30%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.lazy_admin_clearAllNodes'', ''">'', ''Clear node cache'', ''</a>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.login'', ''&amp;u='', ''ADMIN.all'', ''&amp;u='', ''x'', ''">'', ''Login (change user/group)'', ''</a>'', ''</td>'', ''</tr>'', ''</table>'', ''</blockquote>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='all_projects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'all_projects',
 0,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Projects'', ''</h2>'', ''<p'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.new_projects'', ''">'', ''<<??include a=ICON.new'', ''//??>>'', '' New Project'', ''</a>'', ''</p>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<tr'', ''>'', ''<<??include a=ADMIN.projects'', ''&amp;u='', projectid, ''//??>>'', ''</tr>''',
''''', ''</table>''',
'lazy_projects',
'NODEF',
'projectid'
);

delete from lazy_nodes where name='projects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'projects',
 1,
'''''',
''''', ''<td'', '' bgcolor="'', ''#DDDDDD'', ''"'', ''>'', ''<a href="ns?a=ADMIN.del_projects'', ''&amp;u='', projectid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''<a href="ns?a=ADMIN.maj_projects'', ''&amp;u='', projectid as ZZZZ_001, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''</a>'', ''<a href="ns?a=ADMIN.project_details'', ''&amp;u='', projectid as ZZZZ_002, ''">'', ''<<??include a=ICON.definition'', ''//??>>'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', projectid as ZZZZ_003, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=NODE.project'', ''&amp;u='', projectid as ZZZZ_004, ''">'', ''Nodes'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', com, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=ADMIN.all_txt'', ''&amp;u='', <<??param-0//??>>, ''">'', ''Strings'', ''</a>'', ''</td>'', ''<td'', '' bgcolor="'', ''#DDDDDD'', ''"'', ''>'', ''<a href="ns?a=ADMIN.roles_on_project'', ''&amp;u='', projectid as ZZZZ_005, ''">'', ''Access rights'', ''</a>'', ''</td>''',
'''''',
'lazy_projects',
'projectid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='project_details';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'project_details',
 1,
''''', ''<a href="ns?a=ADMIN.all'', ''">'', ''Admin'', ''</a>'', ''<tt'', ''>'', '' -&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.all_projects'', ''">'', ''Projects'', ''</a>'', ''<hr'', ''>'', ''<h2'', ''>'', ''Project '', <<??param-0//??>>, ''</h2>'', ''<p'', ''>'', ''<a href="ns?a=ADMIN.maj_projects'', ''&amp;u='', projectid, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''</a>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_projects'', ''&amp;u='', projectid as ZZZZ_006, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''</p>''',
''''', ''<table'', '' align="'', ''center'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', '' align="'', ''RIGHT'', ''"'', '' width="'', ''27%'', ''"'', ''>'', ''Node type'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', nodetype, ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', '' align="'', ''RIGHT'', ''"'', ''>'', ''Database connection'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', dbconnection, ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', '' align="'', ''RIGHT'', ''"'', ''>'', ''XSLT file'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', xslurl, ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', '' align="'', ''RIGHT'', ''"'', ''>'', ''CSS file'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', cssurl, ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', '' align="'', ''RIGHT'', ''"'', ''>'', ''Background image'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', bkgndurl, ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', '' align="'', ''RIGHT'', ''"'', ''>'', ''Background image'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', bkgndurl as ZZZZ_007, ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''&nbsp;'', ''</td>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=NODE.project'', ''&amp;u='', <<??param-0//??>>, ''">'', ''Nodes'', ''</a>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''&nbsp;'', ''</td>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.roles_on_project'', ''&amp;u='', <<??param-0//??>>, ''">'', ''Access rights'', ''</a>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''&nbsp;'', ''</td>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.all_txt'', ''&amp;u='', <<??param-0//??>>, ''">'', ''Strings'', ''</a>'', ''</td>'', ''</tr>'', ''</table>''',
'''''',
'lazy_projects',
'projectid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='new_projects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_projects',
 0,
''''', ''<a href="ns?a=ADMIN.all'', ''">'', ''Admin'', ''</a>'', ''<tt'', ''>'', '' -&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.all_projects'', ''">'', ''Projects'', ''</a>'', ''<hr'', ''>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_projects"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Project_id   : '', ''<input type="hidden" name="an" value="<<$$projectid$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Description      : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<input type="text" name="av" size="'', 40, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Node type : '', ''<input type="hidden" name="an" value="<<$$nodetype$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', ''html'', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Database connection     : '', ''<input type="hidden" name="an" value="<<$$dbconnection$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', ''DICTLAZY'', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''URL of XSLT file   : '', ''<input type="hidden" name="an" value="<<$$xslurl$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''" value="'', ''xsl/lazy.xsl'', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''URL of CSS file : '', ''<input type="hidden" name="an" value="<<$$cssurl$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''" value="'', ''css/lazy.css'', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''URL of background image : '', ''<input type="hidden" name="an" value="<<$$bkgndurl$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''" value="'', ''bckgnd/neutral.jpg'', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Create'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_projects$$>>"/>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='del_projects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_projects',
 1,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_projects"/>'', ''<input type="submit" name="bidon" value="'', ''Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_projects$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$projectid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', projectid, ''$$>>"/>'', ''<b'', ''>'', '' project '', <<??param-0//??>>, ''</b>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_projects',
'projectid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='maj_projects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'maj_projects',
 1,
''''', ''<a href="ns?a=ADMIN.all'', ''">'', ''Admin'', ''</a>'', ''<tt'', ''>'', '' -&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.all_projects'', ''">'', ''Projects'', ''</a>'', ''<hr'', ''>'', ''<h3'', ''>'', ''Project '', projectid, ''</h3>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_projects"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Description     : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<input type="text" name="av" size="'', 40, ''" value="'', com, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Node type : '', ''<input type="hidden" name="an" value="<<$$nodetype$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', nodetype, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Database connection     : '', ''<input type="hidden" name="an" value="<<$$dbconnection$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', dbconnection, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''URL of XSLT file   : '', ''<input type="hidden" name="an" value="<<$$xslurl$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''" value="'', xslurl, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''URL of CSS file : '', ''<input type="hidden" name="an" value="<<$$cssurl$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''" value="'', cssurl, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''URL of background image : '', ''<input type="hidden" name="an" value="<<$$bkgndurl$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''" value="'', bkgndurl, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_projects$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$projectid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', projectid, ''$$>>"/>'', ''<b'', ''>'', ''  '', <<??param-0//??>>, ''</b>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'lazy_projects',
'projectid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='all_users';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'all_users',
 0,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h1'', ''>'', ''Users'', ''</h1>'', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.new_users'', ''">'', ''<<??include a=ICON.new'', ''//??>>'', ''</a>'', '' '', ''<a href="ns?a=ADMIN.new_users'', ''">'', ''[New]'', ''</a>'', ''</th>'', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''User'', ''</th>'', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Admin?'', ''</th>'', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Default<br>Datagroup'', ''</th>'', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Remark'', ''</th>'', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Roles in Datagroups'', ''</th>''',
''''', ''<tr'', ''>'', ''<<??include a=ADMIN.users'', ''&amp;u='', UserId, ''//??>>'', ''</tr>''',
''''', ''</table>''',
'lazy_users',
'NODEF',
'UserId'
);

delete from lazy_nodes where name='users';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'users',
 1,
'''''',
''''', ''<td'', '' bgcolor="'', ''#DDDDDD'', ''"'', ''>'', ''<a href="ns?a=ADMIN.del_users'', ''&amp;u='', Userid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''<a href="ns?a=ADMIN.maj_users'', ''&amp;u='', Userid as ZZZZ_008, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=ADMIN.user'', ''&amp;u='', userid, ''">'', userid as ZZZZ_009, ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', admin, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', defaultgrpid, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', com, ''</td>'', ''<td'', '' bgcolor="'', ''#DDDDDD'', ''"'', '' align="'', ''center'', ''"'', ''>'', ''<a href="ns?a=ADMIN.grants_of_user'', ''&amp;u='', userid as ZZZZ_0010, ''">'', ''[Roles]'', ''</a>'', ''</td>''',
'''''',
'lazy_users',
'Userid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='user';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'user',
 1,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''User '', <<??param-0//??>>, ''</h2>'', ''<blockquote'', ''>'', com, ''</blockquote>'', ''<blockquote'', ''>'', ''<table'', '' cellspacing="'', ''5'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' align="'', ''right'', ''"'', ''>'', ''Admin: '', ''</td>'', ''<td'', ''>'', ''<b'', ''>'', admin, ''</b>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' align="'', ''right'', ''"'', ''>'', ''Default Data Group: '', ''</td>'', ''<td'', ''>'', ''<a href="ns?a=ADMIN.data_grp'', ''&amp;u='', defaultgrpid, ''">'', defaultgrpid as ZZZZ_0011, ''</a>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' align="'', ''right'', ''"'', ''>'', ''Language: '', ''</td>'', ''<td'', ''>'', lang, ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' align="'', ''right'', ''"'', ''>'', ''Grants: '', ''</td>'', ''<td'', ''>'', ''<a href="ns?a=ADMIN.grants_of_user'', ''&amp;u='', userid, ''">'', ''Roles in Datagroups'', ''</a>'', ''</td>'', ''</tr>'', ''</table>'', ''</blockquote>'', ''<hr'', ''>'', ''<p'', ''>'', ''<a href="ns?a=ADMIN.del_users'', ''&amp;u='', Userid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''[Delete]'', ''</a>'', ''&nbsp;&nbsp;'', ''<a href="ns?a=ADMIN.maj_users'', ''&amp;u='', Userid as ZZZZ_0012, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''[Update]'', ''</a>'', ''</p>''',
'''''',
'''''',
'lazy_users',
'userid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='new_users';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_users',
 0,
''''', ''<a href="ns?a=ADMIN.all'', ''">'', ''Admin'', ''</a>'', ''<tt'', ''>'', '' -&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.all_users'', ''">'', ''Users'', ''</a>'', ''<hr'', ''>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_users"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''User_id      : '', ''<input type="hidden" name="an" value="<<$$userid$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Password     : '', ''<input type="hidden" name="an" value="<<$$encoded|pwd$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Admin        : '', ''<input type="hidden" name="an" value="<<$$admin$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''ADMI'', ''&amp;u='', ''NO'', ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Defaut Data group   : '', ''<input type="hidden" name="an" value="<<$$defaultgrpid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_grpid'', ''&amp;u='', ''PUBLIC'', ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Language         : '', ''<input type="hidden" name="an" value="<<$$lang$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''LANG'', ''&amp;u='', ''eo'', ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<input type="hidden" name="hn" value="<<$$style$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', ''TABLE'', ''$$>>"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''User Info    : '', ''<input type="hidden" name="an" value="<<$$infouser$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 40, ''">'', '''', ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Comment      : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 40, ''">'', '''', ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Insert'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_users$$>>"/>'', ''<b'', ''>'', '' New user'', ''</b>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='maj_users';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'maj_users',
 1,
''''', ''<a href="ns?a=ADMIN.all'', ''">'', ''Admin'', ''</a>'', ''<tt'', ''>'', '' -&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.all_users'', ''">'', ''Users'', ''</a>'', ''<hr'', ''>'', ''<h2'', ''>'', ''Update user '', <<??param-0//??>>, ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.user"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Admin        : '', ''<input type="hidden" name="an" value="<<$$admin$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''ADMI'', ''&amp;u='', admin, ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Defaut Data Group   : '', ''<input type="hidden" name="an" value="<<$$defaultgrpid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_grpid'', ''&amp;u='', defaultgrpid, ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Language        : '', ''<input type="hidden" name="an" value="<<$$lang$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''LANG'', ''&amp;u='', lang, ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<input type="hidden" name="hn" value="<<$$style$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', ''TABLE'', ''$$>>"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''User Info    : '', ''<input type="hidden" name="an" value="<<$$infouser$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 40, ''">'', infouser, ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Comment      : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 40, ''">'', com, ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_users$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$userid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', userid, ''$$>>"/>'', ''<b'', ''>'', '' user '', <<??param-0//??>>, ''</b>'', ''</td>'', ''</tr>'', ''</form>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_pwd_users'', ''&amp;u='', <<??param-0//??>>, ''">'', ''Change Password'', ''</a>'', ''</td>'', ''</tr>''',
''''', ''</table>''',
'lazy_users',
'Userid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='del_users';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_users',
 1,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_users"/>'', ''<input type="submit" name="bidon" value="'', ''Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_users$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$userid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', userid, ''$$>>"/>'', ''<b'', ''>'', '' user '', <<??param-0//??>>, ''</b>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_users',
'userid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='maj_pwd_users';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'maj_pwd_users',
 1,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.maj_users"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''New Password     : '', ''<input type="hidden" name="an" value="<<$$encoded|pwd$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', ''*****'', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Confirm update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_users$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$userid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', userid, ''$$>>"/>'', ''<b'', ''>'', ''for user: '', <<??param-0//??>>, ''</b>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'lazy_users',
'userid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='all_data';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'all_data',
 0,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Data Groups'', ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<<??include a=ADMIN.data_grps'', ''//??>>'', ''<tr'', ''>'', ''<td'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.new_data'', ''">'', ''<<??include a=ICON.new'', ''//??>>'', ''New'', ''</a>'', ''</td>'', ''</tr>'', ''</table>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='data_grps';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'data_grps',
 0,
'''''',
''''', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_data'', ''&amp;u='', grpid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_data'', ''&amp;u='', grpid as ZZZZ_0013, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=ADMIN.data_grp'', ''&amp;u='', grpid as ZZZZ_0014, ''">'', grpid as ZZZZ_0015, ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', com, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=ADMIN.grants_of_group'', ''&amp;u='', grpid as ZZZZ_0016, ''">'', ''Users/Roles'', ''</a>'', ''</td>'', ''</tr>''',
'''''',
'lazy_data',
'grpid<>''*''',
'grpid'
);

delete from lazy_nodes where name='data_grp';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'data_grp',
 1,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Data Group '', <<??param-0//??>>, ''</h2>'', ''<p'', ''>'', com, ''</p>'', ''<blockquote'', ''>'', ''<p'', ''>'', ''<a href="ns?a=ADMIN.grants_of_group'', ''&amp;u='', grpid, ''">'', ''Users and Roles'', ''</a>'', '' on this data group'', ''</p>'', ''</blockquote>'', ''<hr'', ''>'', ''<p'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_data'', ''&amp;u='', grpid as ZZZZ_0017, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''[Update]'', ''</a>'', ''&nbsp;&nbsp;&nbsp;'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_data'', ''&amp;u='', grpid as ZZZZ_0018, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''[Delete]'', ''</a>'', ''</p>''',
'''''',
'''''',
'lazy_data',
'grpid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='new_data';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_data',
 0,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_data"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Data Group Name       : '', ''<input type="hidden" name="an" value="<<$$grpid$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Comment      : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 40, ''">'', '''', ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Insert'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_data$$>>"/>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='del_data';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_data',
 1,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_data"/>'', ''<input type="submit" name="bidon" value="'', ''Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_data$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$grpid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', grpid, ''$$>>"/>'', ''<b'', ''>'', '' '', <<??param-0//??>>, ''</b>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_data',
'grpid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='maj_data';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'maj_data',
 1,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.data_grp"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Comment     : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 40, ''">'', com, ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_data$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$grpid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', grpid, ''$$>>"/>'', ''<b'', ''>'', '' '', <<??param-0//??>>, ''</b>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'lazy_data',
'grpid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='all_roles';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'all_roles',
 0,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Roles'', ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<tr'', ''>'', ''<<??include a=ADMIN.roles'', ''&amp;u='', roleid, ''//??>>'', ''</tr>''',
''''', ''</table>'', ''<p'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.new_roles'', ''">'', ''<<??include a=ICON.new'', ''//??>>'', '' New Role'', ''</a>'', ''</p>''',
'lazy_roles',
'NODEF',
'roleid'
);

delete from lazy_nodes where name='roles';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'roles',
 1,
'''''',
''''', ''<td'', '' bgcolor="'', ''#DDDDDD'', ''"'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_roles'', ''&amp;u='', roleid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_roles'', ''&amp;u='', roleid as ZZZZ_0019, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=ADMIN.role'', ''&amp;u='', roleid as ZZZZ_0020, ''">'', roleid as ZZZZ_0021, ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', com, ''</td>''',
'''''',
'lazy_roles',
'roleid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='role';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'role',
 1,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Role '', <<??param-0//??>>, ''</h2>'', ''<p'', ''>'', com, ''</p>'', ''<blockquote'', ''>'', ''<p'', ''>'', ''<a href="ns?a=ADMIN.all_rolenode'', ''&amp;u='', <<??param-0//??>>, ''">'', ''Access rights'', ''</a>'', ''</p>'', ''<p'', ''>'', ''<a href="ns?a=ADMIN.grants_of_role'', ''&amp;u='', <<??param-0//??>>, ''">'', ''Users and Datagroups'', ''</a>'', ''</p>'', ''</blockquote>'', ''<hr'', ''>'', ''<p'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_roles'', ''&amp;u='', roleid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''[Delete]'', ''</a>'', ''&nbsp;&nbsp;&nbsp;'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_roles'', ''&amp;u='', roleid as ZZZZ_0022, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''[Update]'', ''</a>'', ''</p>''',
'''''',
'''''',
'lazy_roles',
'roleid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='new_roles';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_roles',
 0,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Role_id      : '', ''<input type="hidden" name="an" value="<<$$roleid$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Comment      : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 40, ''">'', '''', ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Create'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_roles$$>>"/>'', ''<b'', ''>'', '' new role'', ''</b>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='del_roles';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_roles',
 1,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all"/>'', ''<input type="submit" name="bidon" value="'', ''Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_roles$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', roleid, ''$$>>"/>'', ''<b'', ''>'', ''  '', <<??param-0//??>>, ''</b>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_roles',
'roleid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='maj_roles';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'maj_roles',
 1,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Comment     : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 40, ''">'', com, ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_roles$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', roleid, ''$$>>"/>'', ''<b'', ''>'', ''  '', <<??param-0//??>>, ''</b>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'lazy_roles',
'roleid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='all_rolenode';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'all_rolenode',
 1,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Access rights of role '', ''<a href="ns?a=ADMIN.role'', ''&amp;u='', <<??param-0//??>>, ''">'', <<??param-0//??>>, ''</a>'', ''</h2>'', ''<table'', '' align="'', ''center'', ''"'', '' width="'', ''70%'', ''"'', ''>'', ''<<??include a=ADMIN.rolenode'', ''&amp;u='', <<??param-0//??>>, ''//??>>'', ''</table>'', ''<p'', ''>'', ''<a href="ns?a=ADMIN.grants_of_role'', ''&amp;u='', <<??param-0//??>>, ''">'', ''Users and Data groups'', ''</a>'', '' with role '', <<??param-0//??>>, ''</p>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='rolenode';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'rolenode',
 1,
''''', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.new_rolenode'', ''&amp;u='', <<??param-0//??>>, ''">'', ''<<??include a=ICON.new'', ''//??>>'', ''New'', ''</a>'', ''</th>'', ''<th'', '' class="'', ''CELL_BG3'', ''"'', ''>'', ''Node(s) or Table'', ''</th>'', ''<th'', '' class="'', ''CELL_BG3'', ''"'', ''>'', ''Access Type'', ''</th>''',
''''', ''<tr'', ''>'', ''<td'', '' bgcolor="'', ''#DDDDDD'', ''"'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_rolenode'', ''&amp;u='', roleid, ''&amp;u='', nodeid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_rolenode'', ''&amp;u='', roleid as ZZZZ_0023, ''&amp;u='', nodeid as ZZZZ_0024, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', '' width="'', ''70%'', ''"'', ''>'', nodeid as ZZZZ_0025, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', typeid, ''</td>'', ''</tr>''',
'''''',
'lazy_rolenode',
'roleid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='roles_on_project';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'roles_on_project',
 1,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Access rights on project '', ''<a href="ns?a=ADMIN.project_details'', ''&amp;u='', <<??param-0//??>>, ''">'', <<??param-0//??>>, ''</a>'', ''</h2>'', ''<table'', '' width="'', ''70%'', ''"'', '' align="'', ''center'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.new_rolenode_for_proj'', ''&amp;u='', <<??param-0//??>>, ''">'', ''<<??include a=ICON.new'', ''//??>>'', ''New'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG3'', ''"'', ''>'', ''Role'', ''</td>'', ''<td'', '' class="'', ''CELL_BG3'', ''"'', ''>'', ''Node(s) or Table'', ''</td>'', ''<td'', '' class="'', ''CELL_BG3'', ''"'', ''>'', ''Access Type'', ''</td>'', ''</tr>'', ''<<??include a=ADMIN.roleproj'', ''&amp;u='', <<??param-0//??>>, ''//??>>'', ''</table>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='roleproj';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'roleproj',
 1,
'''''',
''''', ''<tr'', ''>'', ''<td'', '' bgcolor="'', ''#DDDDDD'', ''"'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_rolenode_for_proj'', ''&amp;u='', roleid, ''&amp;u='', nodeid, ''&amp;u='', <<??param-0//??>>, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_rolenode_for_proj'', ''&amp;u='', roleid as ZZZZ_0026, ''&amp;u='', nodeid as ZZZZ_0027, ''&amp;u='', <<??param-0//??>>, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', roleid as ZZZZ_0028, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', nodeid as ZZZZ_0029, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', typeid, ''</td>'', ''</tr>''',
'''''',
'lazy_rolenode',
'nodeid like concat(<<??param-0//??>>,''.%'')',
'NODEF'
);

delete from lazy_nodes where name='new_rolenode';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_rolenode',
 1,
''''', ''<a href="ns?a=ADMIN.all'', ''">'', ''Admin'', ''</a>'', ''<tt'', ''>'', '' --&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.all_roles'', ''">'', ''Roles'', ''</a>'', ''<tt'', ''>'', '' --&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.all_rolenode'', ''&amp;u='', <<??param-0//??>>, ''">'', <<??param-0//??>>, ''</a>'', ''<hr'', ''>'', ''<h2'', ''>'', ''New Access Right for '', <<??param-0//??>>, ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_rolenode"/>'', ''<input type="hidden" name="u" value="'', ''<<[??ivar-roleid??]>>'', ''"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Role      : '', ''<input type="hidden" name="an" value="<<$$roleid$$>>"/>'', ''<input type="text" name="av" size="'', 20, ''" value="'', <<??param-0//??>>, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Node(s)/Table Id      : '', ''<input type="hidden" name="an" value="<<$$nodeid$$>>"/>'', ''<input type="text" name="av" size="'', 40, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Object Type         : '', ''<input type="hidden" name="an" value="<<$$typeid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''NODETYPE'', ''&amp;u='', ''LAZY'', ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Text      : '', ''<input type="hidden" name="an" value="<<$$lib$$>>"/>'', ''<input type="text" name="av" size="'', 40, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Insert'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_rolenode$$>>"/>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='new_rolenode_for_proj';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_rolenode_for_proj',
 1,
''''', ''<a href="ns?a=ADMIN.all'', ''">'', ''Admin'', ''</a>'', ''<tt'', ''>'', '' --&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.all_projects'', ''">'', ''Projects'', ''</a>'', ''<tt'', ''>'', '' --&gt; '', ''</tt>'', ''<a href="ns?a=ADMIN.project_details'', ''&amp;u='', <<??param-0//??>>, ''">'', <<??param-0//??>>, ''</a>'', ''<hr'', ''>'', ''<h2'', ''>'', ''New Access Right on project '', <<??param-0//??>>, ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.roles_on_project"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Role : '', ''<input type="hidden" name="an" value="<<$$roleid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_roleid'', ''&amp;u='', ''PUBLIC'', ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Node(s)/Table Id : '', ''<input type="hidden" name="an" value="<<$$nodeid$$>>"/>'', ''<input type="text" name="av" size="'', 40, ''" value="'', concat(<<??param-0//??>>,''.''), ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Object Type : '', ''<input type="hidden" name="an" value="<<$$typeid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''NODETYPE'', ''&amp;u='', ''LAZY'', ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Text : '', ''<input type="hidden" name="an" value="<<$$lib$$>>"/>'', ''<input type="text" name="av" size="'', 40, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Insert'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_rolenode$$>>"/>'', ''</td>'', ''</tr>'', ''</form>'', ''</table>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='del_rolenode';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_rolenode',
 2,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_rolenode"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<input type="submit" name="bidon" value="'', ''Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_rolenode$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', roleid, ''$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$nodeid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', nodeid, ''$$>>"/>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_rolenode',
'roleid=<<??param-0//??>> and nodeid=<<??param-1//??>>',
'NODEF'
);

delete from lazy_nodes where name='del_rolenode_for_proj';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_rolenode_for_proj',
 3,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.roles_on_project"/>'', ''<input type="hidden" name="u" value="'', <<??param-2//??>>, ''"/>'', ''<input type="submit" name="bidon" value="'', ''Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_rolenode$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', roleid, ''$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$nodeid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', nodeid, ''$$>>"/>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_rolenode',
'roleid=<<??param-0//??>> and nodeid=<<??param-1//??>>',
'NODEF'
);

delete from lazy_nodes where name='maj_rolenode';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'maj_rolenode',
 2,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_rolenode"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<input type="hidden" name="hn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', <<??param-0//??>>, ''$$>>"/>'', ''<input type="hidden" name="hn" value="<<$$nodeid$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', <<??param-1//??>>, ''$$>>"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Object Type         : '', ''<input type="hidden" name="an" value="<<$$typeid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''NODETYPE'', ''&amp;u='', typeid, ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Text      : '', ''<input type="hidden" name="an" value="<<$$lib$$>>"/>'', ''<input type="text" name="av" size="'', 40, ''" value="'', lib, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_rolenode$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', roleid, ''$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$nodeid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', nodeid, ''$$>>"/>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'lazy_rolenode',
'roleid=<<??param-0//??>> and nodeid=<<??param-1//??>>',
'NODEF'
);

delete from lazy_nodes where name='maj_rolenode_for_proj';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'maj_rolenode_for_proj',
 3,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.roles_on_project"/>'', ''<input type="hidden" name="u" value="'', <<??param-2//??>>, ''"/>'', ''<input type="hidden" name="hn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', <<??param-0//??>>, ''$$>>"/>'', ''<input type="hidden" name="hn" value="<<$$nodeid$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', <<??param-1//??>>, ''$$>>"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Object Type         : '', ''<input type="hidden" name="an" value="<<$$typeid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''NODETYPE'', ''&amp;u='', typeid, ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Text      : '', ''<input type="hidden" name="an" value="<<$$lib$$>>"/>'', ''<input type="text" name="av" size="'', 40, ''" value="'', lib, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_rolenode$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', roleid, ''$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$nodeid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', nodeid, ''$$>>"/>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'lazy_rolenode',
'roleid=<<??param-0//??>> and nodeid=<<??param-1//??>>',
'NODEF'
);

delete from lazy_nodes where name='grants_header';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'grants_header',
 0,
''''', ''<th'', '' bgcolor="'', ''#FFFFBB'', ''"'', ''>'', ''User'', ''</th>'', ''<th'', '' bgcolor="'', ''#FFFFBB'', ''"'', ''>'', ''has Role'', ''</th>'', ''<th'', '' bgcolor="'', ''#FFFFBB'', ''"'', ''>'', ''in Data Group'', ''</th>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='s_grants';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
's_grants',
 4,
'''''',
''''', ''<tr'', ''>'', ''<td'', '' bgcolor="'', ''#DDDDDD'', ''"'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_s_grant'', ''&amp;u='', <<??param-0//??>>, ''&amp;u='', <<??param-1//??>>, ''&amp;u='', <<??param-2//??>>, ''&amp;u='', <<??param-3//??>>, ''&amp;u='', userid, ''&amp;u='', grpid, ''&amp;u='', roleid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=ADMIN.user'', ''&amp;u='', userid as ZZZZ_0030, ''">'', userid as ZZZZ_0031, ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=ADMIN.role'', ''&amp;u='', roleid as ZZZZ_0032, ''">'', roleid as ZZZZ_0033, ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?a=ADMIN.data_grp'', ''&amp;u='', grpid as ZZZZ_0034, ''">'', grpid as ZZZZ_0035, ''</a>'', ''</td>'', ''</tr>''',
'''''',
'lazy_grants',
'(userid=<<??param-1//??>> or <<??param-1//??>>='' '') and (roleid=<<??param-3//??>> or <<??param-3//??>>='' '') and (grpid=<<??param-2//??>> or grpid=''*'' or <<??param-2//??>>='' '')',
'NODEF'
);

delete from lazy_nodes where name='ss_grants';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'ss_grants',
 4,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', <<??param-0//??>>, ''<font'', '' color="'', ''red'', ''"'', ''>'', <<??param-1//??>>, <<??param-2//??>>, <<??param-3//??>>, ''</font>'', ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.new_s_grant'', ''&amp;u='', <<??param-0//??>>, ''&amp;u='', <<??param-1//??>>, ''&amp;u='', <<??param-2//??>>, ''&amp;u='', <<??param-3//??>>, ''">'', ''<<??include a=ICON.new'', ''//??>>'', ''[New]'', ''</a>'', ''</th>'', ''<<??include a=ADMIN.grants_header'', ''//??>>'', ''<<??include a=ADMIN.s_grants'', ''&amp;u='', <<??param-0//??>>, ''&amp;u='', <<??param-1//??>>, ''&amp;u='', <<??param-2//??>>, ''&amp;u='', <<??param-3//??>>, ''//??>>'', ''</table>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='grants_of_user';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'grants_of_user',
 1,
''''', ''<<??include a=ADMIN.ss_grants'', ''&amp;u='', ''Roles granted to user '', ''&amp;u='', <<??param-0//??>>, ''&amp;u='', '' '', ''&amp;u='', '' '', ''//??>>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='grants_of_group';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'grants_of_group',
 1,
''''', ''<<??include a=ADMIN.ss_grants'', ''&amp;u='', ''Users and roles in datagroup '', ''&amp;u='', '' '', ''&amp;u='', <<??param-0//??>>, ''&amp;u='', '' '', ''//??>>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='grants_of_role';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'grants_of_role',
 1,
''''', ''<<??include a=ADMIN.ss_grants'', ''&amp;u='', ''Users and datagroups with role '', ''&amp;u='', '' '', ''&amp;u='', '' '', ''&amp;u='', <<??param-0//??>>, ''//??>>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='new_s_grant';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_s_grant',
 4,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.ss_grants"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<input type="hidden" name="u" value="'', <<??param-1//??>>, ''"/>'', ''<input type="hidden" name="u" value="'', <<??param-2//??>>, ''"/>'', ''<input type="hidden" name="u" value="'', <<??param-3//??>>, ''"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Grant Role          : '', ''<input type="hidden" name="an" value="<<$$roleid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_roleid'', ''&amp;u='', <<??param-3//??>>, ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''to User '', ''<input type="hidden" name="an" value="<<$$userid$$>>"/>'', ''<input type="text" name="av" size="'', 20, ''" value="'', <<??param-1//??>>, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''in Data Group   : '', ''<input type="hidden" name="an" value="<<$$grpid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_grpid'', ''&amp;u='', <<??param-2//??>>, ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Grant'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_grants$$>>"/>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='del_s_grant';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_s_grant',
 7,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.ss_grants"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<input type="hidden" name="u" value="'', <<??param-1//??>>, ''"/>'', ''<input type="hidden" name="u" value="'', <<??param-2//??>>, ''"/>'', ''<input type="hidden" name="u" value="'', <<??param-3//??>>, ''"/>'', ''<input type="submit" name="bidon" value="'', ''Confirm Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_grants$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$userid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', userid, ''$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$grpid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', grpid, ''$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', roleid, ''$$>>"/>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_grants',
'userid=<<??param-4//??>> and roleid=<<??param-6//??>> and grpid=<<??param-5//??>>',
'NODEF'
);

delete from lazy_nodes where name='grants_role';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'grants_role',
 1,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Users with Role '', <<??param-0//??>>, '' in Datagroups'', ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<th'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<a href="ns?a=ADMIN.new_grants'', ''">'', ''New'', ''</a>'', ''</th>'', ''<<??include a=ADMIN.grants_header'', ''//??>>''',
''''', ''<tr'', ''>'', ''<<??include a=ADMIN.grants'', ''&amp;u='', userid, ''&amp;u='', grpid, ''&amp;u='', roleid, ''//??>>'', ''</tr>''',
''''', ''</table>''',
'lazy_grants',
'roleid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='new_grants';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_grants',
 0,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.grants_on_datagroup"/>'', ''<input type="hidden" name="u" value="'', ''[[!userid]]'', ''"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Grant Role          : '', ''<input type="hidden" name="an" value="<<$$roleid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_roleid'', ''&amp;u='', ''PUBLIC'', ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''to User '', ''<input type="hidden" name="an" value="<<$$userid$$>>"/>'', ''<input type="text" name="av" size="'', 20, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''in Data Group   : '', ''<input type="hidden" name="an" value="<<$$grpid$$>>"/>'', ''<select name="av" >'', ''<<??include a=ADMIN.list_grpid'', ''&amp;u='', ''NONE'', ''//??>>'', ''</select>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Grant'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_grants$$>>"/>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='del_grants';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_grants',
 3,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.grants_of_user"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<input type="submit" name="bidon" value="'', ''Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_grants$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$userid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', userid, ''$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$grpid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', grpid, ''$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$roleid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', roleid, ''$$>>"/>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_grants',
'userid=<<??param-0//??>> and roleid=<<??param-2//??>> and grpid=<<??param-1//??>>',
'NODEF'
);

delete from lazy_nodes where name='list_admin_YN';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_admin_YN',
 1,
'''''',
''''', ''<option'', ''>'', decode(''Y'',<<??param-0//??>>,''<selected_option/>'',''''), ''Y'', ''</option>'', ''<option'', ''>'', decode(''N'',<<??param-0//??>>,''<selected_option/>'',''''), ''N'', ''</option>''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='list_grpid';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_grpid',
 1,
''''', ''<<??include a=ADMIN.list_grpid_selected'', ''&amp;u='', <<??param-0//??>>, ''//??>>''',
''''', ''<option'', ''>'', grpid, ''</option>''',
'''''',
'lazy_data',
'grpid<><<??param-0//??>>',
'grpid'
);

delete from lazy_nodes where name='list_grpid_selected';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_grpid_selected',
 1,
'''''',
''''', ''<option'', '' selected="'', ''selected'', ''"'', ''>'', grpid, ''</option>''',
'''''',
'lazy_data',
'grpid=<<??param-0//??>>',
'grpid'
);

delete from lazy_nodes where name='list_roleid';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_roleid',
 1,
''''', ''<<??include a=ADMIN.list_selected_roleid'', ''&amp;u='', <<??param-0//??>>, ''//??>>'', ''<<??include a=ADMIN.list_not_selected_roleid'', ''&amp;u='', <<??param-0//??>>, ''//??>>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='list_selected_roleid';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_selected_roleid',
 1,
'''''',
''''', ''<option'', '' selected="'', ''true'', ''"'', ''>'', roleid, ''</option>''',
'''''',
'lazy_roles',
'roleid=<<??param-0//??>>',
'roleid'
);

delete from lazy_nodes where name='list_not_selected_roleid';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_not_selected_roleid',
 1,
'''''',
''''', ''<option'', ''>'', roleid, ''</option>''',
'''''',
'lazy_roles',
'roleid<><<??param-0//??>>',
'roleid'
);

delete from lazy_nodes where name='list_codeid';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_codeid',
 2,
''''', ''<<??include a=ADMIN.list_codeid_selected'', ''&amp;u='', <<??param-0//??>>, ''&amp;u='', <<??param-1//??>>, ''//??>>'', ''<<??include a=ADMIN.list_codeid_not_selected'', ''&amp;u='', <<??param-0//??>>, ''&amp;u='', <<??param-1//??>>, ''//??>>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='list_codeid_selected';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_codeid_selected',
 2,
'''''',
''''', ''<option'', '' selected="'', ''true'', ''"'', '' value="'', codeid, ''"'', ''>'', abr, ''</option>''',
'''''',
'lazy_codes',
'catid=<<??param-0//??>> and codeid=<<??param-1//??>>',
'abr'
);

delete from lazy_nodes where name='list_codeid_not_selected';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_codeid_not_selected',
 2,
'''''',
''''', ''<option'', '' value="'', codeid, ''"'', ''>'', abr, ''</option>''',
'''''',
'lazy_codes',
'catid=<<??param-0//??>> and codeid<><<??param-1//??>>',
'abr'
);

delete from lazy_nodes where name='list_superlang';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_superlang',
 1,
''''', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''LANG'', ''&amp;u='', <<??param-0//??>>, ''//??>>'', ''<<??include a=ADMIN.list_codeid'', ''&amp;u='', ''STYLE'', ''&amp;u='', <<??param-0//??>>, ''//??>>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='all_connects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'all_connects',
 0,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Database Connections'', ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' withd="'', ''25%'', ''"'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.new_connects'', ''">'', ''<<??include a=ICON.new'', ''//??>>'', ''<b'', ''>'', ''[New]'', ''</b>'', ''</a>'', ''</td>'', ''</tr>''',
''''', ''<tr'', ''>'', ''<<??include a=ADMIN.connects'', ''&amp;u='', connectId, ''//??>>'', ''</tr>''',
''''', ''</table>'', ''<p'', ''>'', ''<a href="ns?a=ADMIN.re_all_connects'', ''">'', ''Re-initialize all connections'', ''</a>'', ''</p>''',
'lazy_connects',
'NODEF',
'connectId'
);

delete from lazy_nodes where name='connects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'connects',
 1,
'''''',
''''', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_connects'', ''&amp;u='', connectid, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''</a>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_connects'', ''&amp;u='', connectid as ZZZZ_0036, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''</a>'', ''<a href="ns?a=ADMIN.connection'', ''&amp;u='', connectid as ZZZZ_0037, ''">'', connectid as ZZZZ_0038, ''</a>'', ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', com, ''</td>'', ''<td'', '' class="'', ''CELL_BG1'', ''"'', ''>'', ''<<??include a=__DBServices.isOK'', ''&amp;u='', <<??param-0//??>>, ''//??>>'', ''</td>''',
'''''',
'lazy_connects',
'connectid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='connection';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'connection',
 1,
''''', ''<<??include a=ADMIN.top'', ''//??>>'', ''<h2'', ''>'', ''Connection '', connectid, ''</h2>'', ''<p'', ''>'', com, ''</p>'', ''<blockquote'', ''>'', ''<p'', ''>'', ''Driver: '', ''<b'', ''>'', driver, ''</b>'', ''</p>'', ''<p'', ''>'', ''URL: '', ''<b'', ''>'', url, ''</b>'', ''</p>'', ''<p'', ''>'', ''User: '', ''<b'', ''>'', userdb, ''</b>'', ''</p>'', ''<p'', ''>'', ''Password: '', ''*****'', ''</p>'', ''<p'', ''>'', ''Connection state: '', ''<b'', ''>'', ''<<??include a=__DBServices.isOK'', ''&amp;u='', <<??param-0//??>>, ''//??>>'', ''</b>'', ''</p>'', ''</blockquote>'', ''<hr'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.del_connects'', ''&amp;u='', connectid as ZZZZ_0039, ''">'', ''<<??include a=ICON.del'', ''//??>>'', ''[Delete]'', ''</a>'', ''&nbsp;&nbsp;&nbsp;'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=ADMIN.maj_connects'', ''&amp;u='', connectid as ZZZZ_0040, ''">'', ''<<??include a=ICON.maj'', ''//??>>'', ''[Update]'', ''</a>''',
'''''',
'''''',
'lazy_connects',
'connectid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='new_connects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'new_connects',
 0,
''''', ''<h2'', ''>'', ''Create a new db connection'', ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_connects"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Connectiom id  : '', ''<input type="hidden" name="an" value="<<$$connectid$$>>"/>'', ''<input type="text" name="av" size="'', 12, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Description   : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 65, ''">'', '''', ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Driver      : '', ''<input type="hidden" name="an" value="<<$$driver$$>>"/>'', ''<input type="text" name="av" size="'', 65, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''URL         : '', ''<input type="hidden" name="an" value="<<$$url$$>>"/>'', ''<input type="text" name="av" size="'', 65, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''User      : '', ''<input type="hidden" name="an" value="<<$$userdb$$>>"/>'', ''<input type="text" name="av" size="'', 20, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Password         : '', ''<input type="hidden" name="an" value="<<$$encoded|pwddb$$>>"/>'', ''<input type="text" name="av" size="'', 15, ''" value="'', '''', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Create'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_connects$$>>"/>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='maj_connects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'maj_connects',
 1,
''''', ''<h2'', ''>'', ''Update connection '', connectid, ''</h2>'', ''<table'', '' width="'', ''100%'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.connection"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Description : '', ''<input type="hidden" name="an" value="<<$$com$$>>"/>'', ''<textarea name="av" rows="'', 3, ''" cols="'', 65, ''">'', com, ''</textarea>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Driver      : '', ''<input type="hidden" name="an" value="<<$$driver$$>>"/>'', ''<input type="text" name="av" size="'', 65, ''" value="'', driver, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''URL         : '', ''<input type="hidden" name="an" value="<<$$url$$>>"/>'', ''<input type="text" name="av" size="'', 65, ''" value="'', url, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''User      : '', ''<input type="hidden" name="an" value="<<$$userdb$$>>"/>'', ''<input type="text" name="av" size="'', 20, ''" value="'', userdb, ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''Password         : '', ''<input type="hidden" name="an" value="<<$$encoded|pwddb$$>>"/>'', ''<input type="text" name="av" size="'', 15, ''" value="'', ''******'', ''"/>'', ''</td>'', ''</tr>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_connects$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$connectid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', connectid, ''$$>>"/>'', ''<b'', ''>'', '' '', connectid as ZZZZ_0041, ''</b>'', ''</td>'', ''</tr>'', ''</form>''',
''''', ''</table>''',
'lazy_connects',
'connectid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='del_connects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'del_connects',
 1,
''''', ''<table'', '' width="'', ''100%'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' class="'', ''CELL_WARNING'', ''"'', ''>''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="ADMIN.all_connects"/>'', ''<input type="submit" name="bidon" value="'', ''Delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$lazy_connects$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$connectid$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', connectid, ''$$>>"/>'', ''<b'', ''>'', '' '', connectid as ZZZZ_0042, ''</b>'', ''</form>''',
''''', ''</td>'', ''</tr>'', ''</table>''',
'lazy_connects',
'connectid=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='re_all_connects';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
're_all_connects',
 0,
''''', ''<<??include a=__DBServices.reInit'', ''//??>>'', ''<hr'', ''>'', ''<<??include a=ADMIN.all_connects'', ''//??>>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='lazy_admin_clearAllNodes';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'lazy_admin_clearAllNodes',
 0,
'''''',
''''', ''<p'', ''>'', ''Cache cleared'', ''</p>'', ''<hr'', ''>'', ''<<??include a=ADMIN.all'', ''//??>>''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='list_usergrpid';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_usergrpid',
 0,
''''', ''<option'', '' selected="'', ''true'', ''"'', ''>'', ''<<[??var-GRP??]>>'', ''</option>''',
'distinct '''', ''<option'', ''>'', grpid, ''</option>''',
'''''',
'lazy_grants',
'userid=''<<[??var-USER??]>>''',
'grpid'
);

delete from lazy_nodes where name='list_usergrpid_selected';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'list_usergrpid_selected',
 0,
'''''',
'distinct '''', ''<option'', ''>'', ''<selected_option/>'', grpid, ''</option>''',
'''''',
'lazy_grants',
'userid=''<<[??var-USER??]>>'' and grpid=''<<[??var-GRP??]>>''',
'grpid'
);
commit;
