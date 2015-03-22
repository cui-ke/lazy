
delete from nodes where name='test_emps';

insert into nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'test_emps',
 0,
''''', ''<p'', ''>'', ''<a href="ns?eip=ZYX0000XYZ&amp;a=EMP2.test_new_emp&amp;u='', ''">'', ''new'', ''</a>'', ''</p>'', ''<hr'', ''>'', ''<table'', '' width="'', ''100%'', ''"'', '' border="'', ''1'', ''"'', '' cellspacing="'', ''2'', ''"'', ''>'', ''<tr'', ''>'', ''<td'', '' width="'', ''80%'', ''"'', ''>'', ''Employee'', ''</td>'', ''<td'', '' width="'', ''20%'', ''"'', ''>'', ''Actions'', ''</td>'', ''</tr>''',
''''', ''<tr'', ''>'', ''<td'', '' valign="'', ''top'', ''"'', ''>'', empno, '' '', ename, '' S='', sal, ''</td>'', ''<td'', ''>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="EMP2.test_emps"/>'', ''<input type="submit" name="bidon" value="'', ''delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$emp$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$empno$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', empno as ZZZZ_001, ''$$>>"/>'', ''</form>'', '' '', ''<a href="ns?a=EMP2.test_many_ops&amp;u='', empno as ZZZZ_002, ''">'', ''test many ops'', ''</a>'', ''  '', ''<a href="ns?a=EMP2.test_confirm_delete_emp&amp;u='', empno as ZZZZ_003, ''">'', ''safe delete'', ''</a>'', ''</td>'', ''</tr>''',
''''', ''</table>'', ''<a href="ns?a=EMP2.test_new_emp&amp;u='', ''">'', ''new'', ''</a>''',
'emp',
'1=1',
'empno'
);

delete from nodes where name='test_many_ops';

insert into nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'test_many_ops',
 1,
'''''',
''''', empno, '' '', ename, ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="EMP2.test_emps"/>'', ''<input type="submit" name="bidon" value="'', ''delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$emp$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$empno$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', empno as ZZZZ_004, ''$$>>"/>'', ''</form>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="EMP2.test_emps"/>'', ''<input type="submit" name="bidon" value="'', ''increase 1.5'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$emp$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$empno$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', empno as ZZZZ_005, ''$$>>"/>'', ''<input type="hidden" name="hn" value="<<$$sal$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', sal*1.5, ''$$>>"/>'', ''</form>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="EMP2.test_emps"/>'', ''<input type="submit" name="bidon" value="'', ''increase 2.5'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$emp$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$empno$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', empno as ZZZZ_006, ''$$>>"/>'', ''<input type="hidden" name="hn" value="<<$$sal$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', sal*2.5, ''$$>>"/>'', ''</form>'', ''<hr'', ''>''',
'''''',
'emp',
'empno=<<??param-0//??>>',
'1'
);

delete from nodes where name='test_increase';

insert into nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'test_increase',
 2,
'''''',
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="EMP2.test_emps"/>'', ''<input type="submit" name="bidon" value="'', ''increase'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$emp$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$empno$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', empno, ''$$>>"/>'', ''<input type="hidden" name="hn" value="<<$$sal$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', <<??param-1//??>>, ''$$>>"/>'', ''</form>''',
'''''',
'emp',
'empno=<<??param-0//??>>',
'1'
);

delete from nodes where name='test_new_emp';

insert into nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'test_new_emp',
 0,
''''', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="EMP2.test_emps"/>'', ''no: '', ''<input type="hidden" name="an" value="<<$$empno$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', ''nnn'', ''"/>'', ''name: '', ''<input type="hidden" name="an" value="<<$$ename$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''" value="'', ''mmm'', ''"/>'', ''<input type="submit" name="bidon" value="'', ''insert'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$emp$$>>"/>'', ''</form>''',
''' ''',
''' ''',
'dual',
'1=1',
'1'
);

delete from nodes where name='test_confirm_delete_emp';

insert into nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'test_confirm_delete_emp',
 1,
''''', ''no: '', empno, ''name: '', ename, ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="EMP2.test_emps"/>'', ''<input type="submit" name="bidon" value="'', ''confirm delete'', ''"/>'', ''<input type="hidden" name="act" value="<<$$del$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$emp$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$empno$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', empno as ZZZZ_007, ''$$>>"/>'', ''</form>'', ''<a href="ns?a=EMP2.test_emps&amp;u='', ''">'', ''DON''''T'', ''</a>''',
''' ''',
''' ''',
'emp',
'empno=<<??param-0//??>>',
'1'
);
commit;
