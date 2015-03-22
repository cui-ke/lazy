
delete from lazy_nodes where name='test_from';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'test_from',
 0,
'''<hr><blockquote>node TEST.<b>test_from</b> has compilation errors</blockquote><hr>''',
'''''',
'''''',
'DUAL',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='test_from_2';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'test_from_2',
 0,
'''<hr><blockquote>node TEST.<b>test_from_2</b> has compilation errors</blockquote><hr>''',
'''''',
'''''',
'DUAL',
'NODEF',
'NODEF'
);

delete from lazy_nodes where name='test_from_3';

insert into lazy_nodes(name, nbparam, pre, items, post, collection, selector, ordering)
values(
'test_from_3',
 0,
'''<hr><blockquote>node TEST.<b>test_from_3</b> has compilation errors</blockquote><hr>''',
'''''',
'''''',
'DUAL',
'NODEF',
'NODEF'
);
commit;
