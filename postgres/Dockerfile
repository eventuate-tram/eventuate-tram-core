FROM postgres:9.6

ENV WAL2JSON_COMMIT_ID=d4c0e814696695bbf853c48b38b7479e0f83f6c7

# Install the packages which will be required to get everything to compile
RUN  apt-get update  && apt-get -y install git build-essential postgresql-server-dev-9.6

RUN git clone https://github.com/eulerto/wal2json -b master --single-branch \
    && cd /wal2json \
    && git checkout $WAL2JSON_COMMIT_ID \
    && make && make install \
    && cd / \
    && rm -rf wal2json \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy the custom configuration which will be passed down to the server (using a .sample file is the preferred way of doing it by
# the base Docker image)
COPY postgresql.conf.sample /usr/share/postgresql/postgresql.conf.sample

# Copy the script which will initialize the replication permissions
COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

# Initialize schema
COPY initialize-database.sql /docker-entrypoint-initdb.d