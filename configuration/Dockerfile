FROM baseimagesrepo/java:1.8.0

# Copy and unpack Liquibase
ADD database/lib/liquibase-3.6.2-bin.tar.gz /opt/liquibase

# Copy migration script
COPY database/scripts/migrate.sh /opt/liquibase

# Copy database drivers
COPY database/src/main/resources/drivers /opt/liquibase/lib

# Copy changelogs
COPY database/src/main/resources/changelogs /opt/liquibase/changelogs
COPY database/src/main/resources/migrations.xml /opt/liquibase

RUN chmod +x /opt/liquibase/liquibase && ln -s /opt/liquibase/liquibase /usr/local/bin/ && sed -i 's/bash/sh/' /opt/liquibase/liquibase && chmod +x /opt/liquibase/migrate.sh

WORKDIR /opt/liquibase

CMD ["/bin/sh", "migrate.sh"]
