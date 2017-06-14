#!/bin/sh

# $1 is the report directory with the trailing /

mkdir $1data;

awk '/CPU/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_cpu;
awk '/proc\/s/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_proc;
awk '/pgpgin\/s/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_pagination;
awk '/tps/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_io;
awk '/kbmemfree/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_memory;
awk '/IFACE/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_network;
awk '/totsck/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_sockets;
awk '/MBfsfree/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_storage;

awk '/CPU/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_cpu;
awk '/proc\/s/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_proc;
awk '/pgpgin\/s/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_pagination;
awk '/tps/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_io;
awk '/kbmemfree/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_memory;
awk '/IFACE/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_network;
awk '/totsck/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_sockets;
awk '/MBfsfree/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_storage;

awk '/CPU/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_cpu;
awk '/proc\/s/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_proc;
awk '/pgpgin\/s/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_pagination;
awk '/tps/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_io;
awk '/kbmemfree/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_memory;
awk '/IFACE/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_network;
awk '/totsck/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_sockets;
awk '/MBfsfree/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_storage;
