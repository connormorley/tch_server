drop table if exists wordlist;
drop table if exists attack_information;
drop table if exists failed_sequences;
drop table if exists arn_sequences;

create table wordlist(
ID int not null auto_increment,
password varchar(250) unique,
primary key(ID)
);

create table attack_information(
attack_id int unique,
balance_value int,
attack_method varchar(100),
attack_start long,
attack_running varchar(10),
attack_stop long,
attack_result varchar(100),
primary key (attack_id)
);

create table failed_sequences(
attack_id int,
failed_arn int unique
);

create table arn_sequences(
attack_id int,
arn int unique,
devid varchar(250)
);
