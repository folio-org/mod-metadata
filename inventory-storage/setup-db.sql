DROP DATABASE IF EXISTS folio_shared;
DROP ROLE IF EXISTS testuser;

CREATE ROLE testuser PASSWORD 'test' NOSUPERUSER CREATEDB INHERIT LOGIN;
CREATE DATABASE folio_shared WITH OWNER = testuser