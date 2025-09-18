CREATE DATABASE integration
exec sp_configure 'contained database authentication', 1;
RECONFIGURE ;
USE integration
ALTER DATABASE integration SET CONTAINMENT = PARTIAL;
