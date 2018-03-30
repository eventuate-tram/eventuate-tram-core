FROM postgres:9.6

ENV PLUGIN_VERSION=v0.7.0
ENV WAL2JSON_COMMIT_ID=f68cb0096c669a2ee5a2d32b54a535513e3cb23b

# Install the packages which will be required to get everything to compile
RUN apt-get update \
    && apt-get install -f -y --no-install-recommends \
        software-properties-common \
        build-essential \
        pkg-config \ 
        git \
        postgresql-server-dev-9.6 \
        libproj-dev \
    && apt-get clean && apt-get update && apt-get install -f -y --no-install-recommends \            
        liblwgeom-dev \              
    && add-apt-repository "deb http://ftp.debian.org/debian testing main contrib" \ 
    && apt-get install nano \
    && apt-get update && apt-get install -f -y --no-install-recommends \
        libprotobuf-c-dev=1.2.* \
    && rm -rf /var/lib/apt/lists/*             

RUN git clone https://github.com/eulerto/wal2json -b master --single-branch \
    && cd /wal2json \
    && git checkout $WAL2JSON_COMMIT_ID \
    && make && make install \
    && cd / \
    && rm -rf wal2json

# Copy the custom configuration which will be passed down to the server (using a .sample file is the preferred way of doing it by
# the base Docker image)
COPY postgresql.conf.sample /usr/share/postgresql/postgresql.conf.sample

# Copy the script which will initialize the replication permissions
COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

# Initialize schema
COPY initialize-database.sql /docker-entrypoint-initdb.d
