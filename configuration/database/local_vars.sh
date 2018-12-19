#!/usr/bin/env bash
export DATABASE_DB_USER=SA
export DATABASE_DB_PASSWORD=dockerPassword1!
export DATABASE_DB_CONNECTION_STRING="jdbc:sqlserver://${DATABASE_DB_HOST}:${DATABASE_DB_PORT};database=${DATABASE_DB_NAME}"
export BASE_SERVICE_DB_USER=certificateServiceUser
export BASE_SERVICE_DB_PASSWORD=baseServicePassword1!
export BASE_SERVICE_TABLE_NAME=importnotification

export SERVICE_PORT=4861
export SERVICE_USER=importer
export SERVICE_PASSWORD=password123
