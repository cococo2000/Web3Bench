#!/bin/bash

# Values to modify
###########################################################
is_tidb_server=true
new_ip='127.0.0.1'
new_port=4000
new_dbname=web3bench
new_username=root
new_password=''
new_scalefactor=3
# Test time in minutes
new_time=5
###########################################################

# Create ~/mysql.cnf file
mysql_config_file=~/mysql.cnf
echo "[client]" > $mysql_config_file
echo "user=$new_username" >> $mysql_config_file
echo "password=$new_password" >> $mysql_config_file

# Create database
echo "Creating database $new_dbname if not exists"
mysql --defaults-extra-file=$mysql_config_file -h $new_ip -P $new_port -e "CREATE DATABASE IF NOT EXISTS $new_dbname;"

# When using TiDB, we need to set tidb_skip_isolation_level_check=1 to disable the isolation level check.
if [ $is_tidb_server = true ] ; then
    echo "TiDB server detected, setting tidb_skip_isolation_level_check=1"
    mysql --defaults-extra-file=$mysql_config_file -h $new_ip -P $new_port -e "set global tidb_skip_isolation_level_check=1;"
fi

# Delete $mysql_config_file file
rm $mysql_config_file

# List of files to process
files=("loaddata.xml" 
        "runR21.xml" 
        "runR22.xml" 
        "runR23.xml" 
        "runR24.xml" 
        "runthread1.xml" 
        "runthread2.xml"
)

# Modify config files
echo "Modifying config files with new values"
# New DBUrl
new_dburl="jdbc:mysql://$new_ip:$new_port/$new_dbname?useSSL=false\&amp;characterEncoding=utf-8"
echo "###########################################################"
echo "New DBUrl: $new_dburl"
echo "New username: $new_username"
echo "New password: $new_password"
echo "New scalefactor: $new_scalefactor"
echo "New time: $new_time"
echo "###########################################################"

for file in "${files[@]}"; do
    if [ -f "../config/$file" ]; then
        sed -i"" "s#<DBUrl>.*</DBUrl>#<DBUrl>$new_dburl</DBUrl>#g" "../config/$file"
        sed -i"" "s#<username>.*</username>#<username>$new_username</username>#g" "../config/$file"
        sed -i"" "s#<password>.*</password>#<password>$new_password</password>#g" "../config/$file"
        sed -i"" "s#<scalefactor>.*</scalefactor>#<scalefactor>$new_scalefactor</scalefactor>#g" "../config/$file"
        sed -i"" "s#<time>.*</time>#<time>$new_time</time>#g" "../config/$file"
        echo -e "\tFile $file modified"
    else
        echo -e "\tFile $file doesn't exist"
    fi
done

echo "All config files modified"
