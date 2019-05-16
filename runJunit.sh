#!/bin/sh
mvn test -f service/pom.xml --settings service/vs-maven.xml -Dmaven.repo.local=${WORKSPACE}/.m2