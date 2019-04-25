#run the setup scripts to create the DB and the schema in the DB

until (echo select 1 from dual | /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Eventuate123! -d master > /dev/null)
do
 echo sleeping for mssql
 sleep 5
done


for i in `ls *.sql | sort -V`; do
	/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Eventuate123! -d master -i "$i"
done;
