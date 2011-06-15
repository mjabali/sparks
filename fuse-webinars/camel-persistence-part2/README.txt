<!--
  ~ Copyright 2011 FuseSource
  ~
  ~    Licensed under the Apache License, Version 2.0 (the "License");
  ~    you may not use this file except in compliance with the License.
  ~    You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~    Unless required by applicable law or agreed to in writing, software
  ~    distributed under the License is distributed on an "AS IS" BASIS,
  ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~    See the License for the specific language governing permissions and
  ~    limitations under the License.
  -->

DESCRIPTION
===========

This is the material accompanying the presentation of part I - Database persistence with Camel from simple to more elaborated.
It covers the different demo made during the talk and is organized like that :

database = project containing the scripts to create the database in HSQLDB and the jar of the HSQLDB server
jdbc = maven project for camel-jdbc demo
sql = idem but for camel-sql component
jpa = mavemn project containing camel routes for JPA persistence
sql-spring-persistence = maven project with additional examples (not covered during the webinars) but could be used to test transaction with SQL component


DATABASE
========

STEP 1 : Open a DOS/UNIX console in the folder persistence/database

STEP 2 : Start the HSQLDB Server using the command

java -cp lib/hsqldb-1.8.0.10.jar org.hsqldb.Server -database.0 file:reportdb -dbname.0 reportdb

STEP 3 : In a separate DOS/UNIX console, start the Swing DataBase Manager console using the following command

java -cp lib/hsqldb-1.8.0.10.jar org.hsqldb.util.DatabaseManagerSwing --user sa --url jdbc:hsqldb:hsql://localhost/reportdb

STEP 4 : Create Schema and Tables using the script located in the file persistence/database/src/config/hsqldb/reportdb-scripts.sql

Execute the scripts 1), 2) and 3) defined in this file

MAVEN
=====

Except the database directory containing, the other folders are maven projects. The webinar
part1 can be build using the command : mvn clean install in the persistence folder.

To launch each project individually, simply execute the following command in a DOS/UNIX console

mvn camel:run

Depending in which project you are (jdbc, sql or jpa), you will have to copy files
to allow the file:// endpoint of the camel routes to read the corresponding file (key.txt, keys.txt or csv.txtx) and insert data
into the database


1) route-two-tx folder
Charles-Moulliards-MacBook-Pro:route-two-tx charlesmoulliard$ cp data/csv.txt /Users/charlesmoulliard/Applications/apache-servicemix-4.3.1-fuse-01-09/datainsert
Charles-Moulliards-MacBook-Pro:route-two-tx charlesmoulliard$ pwd
/Users/charlesmoulliard/fuse/sparks/fuse-webinars/camel-persistence-part2/route-two-tx


Features
========

features:addUrl mvn:com.fusesource.webinars/persistence/1.0.0/xml/features

1) Install Projet
features:install reportincident-jpa

Check that two lines are uncommented in the the file /etc/jre.properties for JDK 1.6

 javax.annotation, \
 javax.annotation.processing, \


 drop table `PUBLIC`.`OPENJPA_SEQUENCE_TABLE`;
drop table `PUBLIC`.`T_INCIDENT`;
