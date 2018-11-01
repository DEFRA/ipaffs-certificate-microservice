# Integration


This project is a standalone maven project for running automated integration tests.


- JRE / JDK 8
- Maven v3
- [Lombok Plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin) (only required for development)


(Cent OS)

- If there is no existing JDK, download and install 
  [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/install-linux-64-rpm-138254.html)



Example VM Options for IntelliJ

    -Dskip.integration.tests=false
    -Dit.test=WIPTestRunner
    -Dservice.base.url=http://localhost:4862
    -Dauth.username=importer
    -Dauth.password=password123
    -Denvironment.name=local
    -Dbranch.prefix=imta-3160