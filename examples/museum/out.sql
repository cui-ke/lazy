
delete from lazy_nodes where name='mw_new_Work';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'mw_new_Work',
 0,
''''', ''<h2'', ''>'', ''Adding a new work'', ''</h2>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="MW.mw_Work_index"/>'', ''<p'', ''>'', ''Title: '', ''<input type="hidden" name="an" value="<<$$title$$>>"/>'', ''<input type="text" name="av" size="'', 60, ''"/>'', ''</p>'', ''<p'', ''>'', ''Author: '', ''<input type="hidden" name="an" value="<<$$author$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''"/>'', ''<font'', '' color="'', ''red'', ''"'', ''>'', '' (artist no.)'', ''</font>'', ''</p>'', ''<p'', ''>'', ''Support: '', ''<input type="hidden" name="an" value="<<$$support$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''"/>'', ''</p>'', ''<p'', ''>'', ''Date: '', ''<input type="hidden" name="an" value="<<$$c_date$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''"/>'', ''</p>'', ''<p'', ''>'', ''Height: '', ''<input type="hidden" name="an" value="<<$$height$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''"/>'', ''</p>'', ''<p'', ''>'', ''Width: '', ''<input type="hidden" name="an" value="<<$$Width$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''"/>'', ''</p>'', ''<p'', ''>'', ''Picture location: '', ''<input type="hidden" name="an" value="<<$$picture$$>>"/>'', ''<input type="text" name="av" size="'', 50, ''"/>'', '' (URL)'', ''</p>'', ''<input type="submit" name="bidon" value="'', ''Add'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$work$$>>"/>'', ''</form>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='mw_add_work_by_artist';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'mw_add_work_by_artist',
 2,
''''', ''<h2'', ''>'', ''Adding a new work'', ''</h2>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="MW.mw_Artist"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<p'', ''>'', ''Author: '', <<??param-1//??>>, ''<input type="hidden" name="hn" value="<<$$author$$>>"/>'', ''<input type="hidden" name="hv" value="<<$$'', <<??param-0//??>>, ''$$>>"/>'', ''</p>'', ''<p'', ''>'', ''Title: '', ''<input type="hidden" name="an" value="<<$$title$$>>"/>'', ''<input type="text" name="av" size="'', 60, ''"/>'', ''</p>'', ''<p'', ''>'', ''Support: '', ''<input type="hidden" name="an" value="<<$$support$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''"/>'', ''</p>'', ''<p'', ''>'', ''Date: '', ''<input type="hidden" name="an" value="<<$$c_date$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''"/>'', ''</p>'', ''<p'', ''>'', ''Height: '', ''<input type="hidden" name="an" value="<<$$height$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''"/>'', ''</p>'', ''<p'', ''>'', ''Width: '', ''<input type="hidden" name="an" value="<<$$Width$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''"/>'', ''</p>'', ''<p'', ''>'', ''Picture location: '', ''<input type="hidden" name="an" value="<<$$picture$$>>"/>'', ''<input type="text" name="av" size="'', 50, ''"/>'', '' (URL)'', ''</p>'', ''<input type="submit" name="bidon" value="'', ''Add'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$work$$>>"/>'', ''</form>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='mw_upd_Work';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'mw_upd_Work',
 1,
''''', ''<h2'', ''>'', ''Updating a work'', ''</h2>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="MW.mw_Work"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<p'', ''>'', ''Unique identifier: '', wno, ''</p>'', ''<p'', ''>'', ''Title: '', ''<input type="hidden" name="an" value="<<$$title$$>>"/>'', ''<input type="text" name="av" size="'', 60, ''" value="'', title, ''"/>'', ''</p>'', ''<p'', ''>'', ''Author: '', ''<input type="hidden" name="an" value="<<$$author$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', author, ''"/>'', ''<font'', '' color="'', ''red'', ''"'', ''>'', '' (artist no.)'', ''</font>'', ''</p>'', ''<p'', ''>'', ''Support: '', ''<input type="hidden" name="an" value="<<$$support$$>>"/>'', ''<input type="text" name="av" size="'', 30, ''" value="'', support, ''"/>'', ''</p>'', ''<p'', ''>'', ''Date: '', ''<input type="hidden" name="an" value="<<$$c_date$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', c_date, ''"/>'', ''</p>'', ''<p'', ''>'', ''Height: '', ''<input type="hidden" name="an" value="<<$$height$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', height, ''"/>'', ''</p>'', ''<p'', ''>'', ''Width: '', ''<input type="hidden" name="an" value="<<$$Width$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', Width, ''"/>'', ''</p>'', ''<p'', ''>'', ''Picture location: '', ''<input type="hidden" name="an" value="<<$$picture$$>>"/>'', ''<input type="text" name="av" size="'', 50, ''" value="'', picture, ''"/>'', '' (URL)'', ''</p>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$work$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$wno$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', wno as ZZZZ_001, ''$$>>"/>'', ''</form>''',
'''''',
'''''',
'work',
'wno=<<??param-0//??>>',
'NODEF'
);

delete from lazy_nodes where name='mw_new_Artist';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'mw_new_Artist',
 0,
''''', ''<h2'', ''>'', ''Adding a new artist'', ''</h2>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="MW.mw_Artist_index"/>'', ''<p'', ''>'', ''Name: '', ''<input type="hidden" name="an" value="<<$$name$$>>"/>'', ''<input type="text" name="av" size="'', 60, ''"/>'', ''</p>'', ''<p'', ''>'', ''Birthdate: '', ''<input type="hidden" name="an" value="<<$$birthdate$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', ''1800'', ''"/>'', '' mandatory'', ''</p>'', ''<p'', ''>'', ''Deathdate: '', ''<input type="hidden" name="an" value="<<$$deathdate$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', ''1900'', ''"/>'', '' mandatory'', ''</p>'', ''<input type="submit" name="bidon" value="'', ''Add'', ''"/>'', ''<input type="hidden" name="act" value="<<$$new$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$artist$$>>"/>'', ''</form>''',
'''''',
'''''',
'dual',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='mw_upd_Artist';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'mw_upd_Artist',
 1,
''''', ''<h2'', ''>'', ''Updating an artist description'', ''</h2>'', ''<form action="ns" method="post">'', ''<input type="hidden" name="a" value="MW.mw_Artist"/>'', ''<input type="hidden" name="u" value="'', <<??param-0//??>>, ''"/>'', ''<p'', ''>'', ''Unique identifier: '', ano, ''</p>'', ''<p'', ''>'', ''Name: '', ''<input type="hidden" name="an" value="<<$$name$$>>"/>'', ''<input type="text" name="av" size="'', 60, ''" value="'', name, ''"/>'', ''</p>'', ''<p'', ''>'', ''Birthdate: '', ''<input type="hidden" name="an" value="<<$$birthdate$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', birthdate, ''"/>'', ''</p>'', ''<p'', ''>'', ''Deathdate: '', ''<input type="hidden" name="an" value="<<$$deathdate$$>>"/>'', ''<input type="text" name="av" size="'', 10, ''" value="'', deathdate, ''"/>'', ''</p>'', ''<input type="submit" name="bidon" value="'', ''Update'', ''"/>'', ''<input type="hidden" name="act" value="<<$$upd$$>>"/><input  type="hidden" name="con" value="<<$$DICTLAZY$$>>"/>'', ''<input type="hidden" name="tbl" value="<<$$artist$$>>"/>'', ''<input type="hidden" name="kn" value="<<$$ano$$>>"/>'', ''<input type="hidden" name="kv" value="<<$$'', ano as ZZZZ_002, ''$$>>"/>'', ''</form>''',
'''''',
'''''',
'artist',
'ano=<<??param-0//??>>',
'NODEF'
);
commit;
