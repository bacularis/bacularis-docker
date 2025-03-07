#!/bin/bash

POSTGRESQL_USER='postgres'
BACULA_DBNAME='bacula'
BACULA_DBUSER='bacula'
BACULA_DBPASSWORD='bacula'
BACULA_USER='bacula'

function start_postgresql()
{
	/etc/init.d/postgresql start
}

function stop_postgresql()
{
	/etc/init.d/postgresql stop
}

function init_bacula()
{
    su ${POSTGRESQL_USER} -c "db_name='${BACULA_DBNAME}' /usr/share/bacula-director/create_postgresql_database"
    su ${POSTGRESQL_USER} -c "db_name='${BACULA_DBNAME}' /usr/share/bacula-director/make_postgresql_tables"
    su ${POSTGRESQL_USER} -c "db_name='${BACULA_DBNAME}' db_user='${BACULA_DBUSER}'  db_password='${BACULA_DBPASSWORD}' /usr/share/bacula-director/grant_postgresql_privileges"
}

function start_bacula_dir()
{
    if ! su ${POSTGRESQL_USER} -c 'psql -l' | grep '^ bacula' 1>/dev/null
    then
        init_bacula
    fi

    /etc/init.d/bacula-director start
}

function stop_bacula_dir()
{
    /etc/init.d/bacula-director stop
}

function start_bacula_sd()
{
    /etc/init.d/bacula-sd start
}

function stop_bacula_sd()
{
    /etc/init.d/bacula-sd stop
}

function start_bacula_fd()
{
    /etc/init.d/bacula-fd start
}

function stop_bacula_fd()
{
    /etc/init.d/bacula-fd stop
}

function start_php_fpm()
{
    /etc/init.d/php8.4-fpm start
}

function stop_php_fpm()
{
    /etc/init.d/php8.4-fpm stop
}
