FROM mcr.microsoft.com/mssql/server:2017-latest

WORKDIR /usr/src/app
CMD /bin/bash ./entrypoint.sh
EXPOSE 1433

COPY . /usr/src/app
RUN chmod +x /usr/src/app/create-schema.sh