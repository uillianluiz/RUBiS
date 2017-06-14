#!/bin/sh

# $1 is the report directory with the trailing /

mkdir $1data

awk '/CPU/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_cpu.csv;
awk '/proc\/s/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_proc.csv;
awk '/pgpgin\/s/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_pagination.csv;
awk '/tps/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_io.csv;
awk '/kbmemfree/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_memory.csv;
awk '/IFACE/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_network.csv;
awk '/totsck/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_sockets.csv;
awk '/MBfsfree/{getline; print}' $1client0 | sed '1d; $d'  > $1data/client0_storage.csv;

awk '/CPU/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_cpu.csv;
awk '/proc\/s/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_proc.csv;
awk '/pgpgin\/s/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_pagination.csv;
awk '/tps/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_io.csv;
awk '/kbmemfree/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_memory.csv;
awk '/IFACE/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_network.csv;
awk '/totsck/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_sockets.csv;
awk '/MBfsfree/{getline; print}' $1db_server | sed '1d; $d'  > $1data/db_server_storage.csv;

awk '/CPU/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_cpu.csv;
awk '/proc\/s/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_proc.csv;
awk '/pgpgin\/s/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_pagination.csv;
awk '/tps/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_io.csv;
awk '/kbmemfree/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_memory.csv;
awk '/IFACE/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_network.csv;
awk '/totsck/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_sockets.csv;
awk '/MBfsfree/{getline; print}' $1web_server | sed '1d; $d'  > $1data/web_server_storage.csv;
