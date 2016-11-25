-- CREATE DATABASE xapp DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
drop table IF EXISTS t_primary_key;
create table t_primary_key(
    table_name varchar(32) not null primary key,
    current_id int not null
)ENGINE = INNODB;

drop table IF EXISTS t_user;
create table t_user(
    id int not null primary key,
    name VARCHAR(50),
    password VARCHAR(32),
    mail varchar(20)
)ENGINE = INNODB;

drop table IF EXISTS t_role;
create table t_role(
    id int not null primary key,
    name VARCHAR(50)
)ENGINE = INNODB;

drop table IF EXISTS t_mod;
create table t_mod(
    id int not null primary key,
    name VARCHAR(50),
    url varchar(50),
    router_id varchar(50),
    addition VARCHAR(50),
    parent_id int
)ENGINE = INNODB;

drop table IF EXISTS t_user_role;
create table t_user_role(
    id int AUTO_INCREMENT primary key,
    user_id int not null,
    role_id int not null
)ENGINE = INNODB;

drop table IF EXISTS t_role_mod;
create table t_role_mod(
    id int AUTO_INCREMENT primary key,
    role_id int not null,
    mod_id int not null
)ENGINE = INNODB;

drop table IF EXISTS t_dynamic_conf;
create table t_dynamic_conf(
    conf_name varchar(60) primary key,
    conf_value varchar(128),
    conf_desc varchar(128),
    dirty tinyint
)ENGINE = INNODB;

drop table IF EXISTS t_event;
create table t_event(
    event_id int auto_increment primary key,
    event_type tinyint not null,
    db_key int not null,
    db_int int not null,
    db_content varchar(32),
    event_status tinyint not null,
    try_count tinyint not null,
    trigger_date DATETIME not null
)ENGINE = INNODB;

insert into t_dynamic_conf(conf_name, conf_value, conf_desc, dirty) VALUE ('appUpdating','false','升级升级前，请将此参数改为true',0);

insert into t_user values(-10,'a','0cc175b9c0f1b6a831c399e269772661','a@a.com');
insert into t_user values(-9,'g','b2f5ff47436671b6e533d8dc3614845d','g@g.com');

insert into t_role values(-2,'administrator');
insert into t_role values(-1,'guest');

insert into t_user_role values(-2,-10,-2);-- administrator
insert into t_user_role values(-1,-9,-1); -- guest

insert into t_mod values(-100,'系统配置',null,null,'x-fa fa-user-secret',0);
insert into t_mod values(-99,'用户管理',null,'user-UserManager','x-fa fa-user',-100);
insert into t_mod values(-98,'动态参数',null,'user-DynamicConfig','x-fa fa-pencil',-100);
-- insert into t_mod values(-98,'权限管理',null,'user-RoleManager','x-fa fa-user-secret',-100);
-- insert into t_mod values(-97,'角色管理',null,'user-RoleManager','x-fa fa-user-secret',-100);

insert into t_role_mod values(-10,-2,-100);
insert into t_role_mod values(-9,-2,-99);
insert into t_role_mod values(-8,-2,-98);

alter table t_role add unique key `uni_name` (`name`);




