drop table graphes;
create table graphes(graph varchar2(16)  primary key,
                     lib varchar2(1000));


drop table graphnodes;
create table graphnodes(node varchar2(16) not null,
                       graph varchar2(16) not null references graphes(graph),
                       primary key (graph ,node ));


drop table graphedges;
create table graphedges(nfrom varchar2(16) not null,
                   nto varchar2(16) not null ,
                   graph varchar2(16) not null,
                   length number(3) ,
                   primary key (graph ,nfrom, nto ),
                   check(nto>=nfrom),
                   foreign key (graph,nfrom) references graphnodes(graph,node),
                   foreign key (graph,nto) references graphnodes(graph,node)
                   );
