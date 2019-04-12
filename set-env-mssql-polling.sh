. ./_set-env.sh

export SPRING_DATASOURCE_URL="jdbc:sqlserver://172.17.0.1:1433;databaseName=eventuate"
export SPRING_DATASOURCE_USERNAME=sa
export SPRING_DATASOURCE_PASSWORD=Eventuate123!
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.microsoft.sqlserver.jdbc.SQLServerDriver
export EVENTUATELOCAL_CDC_READER_NAME=MssqlPollingReader
export SPRING_PROFILES_ACTIVE=EventuatePolling

