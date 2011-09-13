# Fuse Archetype - Camel Workshop

## Description

Fuse Archetype - Camel Workshop is a maven archetype allowing to generate a maven project structure
to develop a POC. More info can be find here about the project - https://github.com/fusesource/sparks/tree/master/fuse-archetypes/camel-workshop/src/main/resources/archetype-resources

## How to use it

- Simply execute the following command in a shell environment

mvn archetype:generate -DarchetypeGroupId=org.fusesource.sparks.archetypes -DarchetypeArtifactId=camel-wks-archetype -DarchetypeVersion=1.0-SNAPSHOT -DgroupId=com.fusesource.workshop -DartifactId=poc -Dversion=1.0

- Go to poc directory and run a mvn clean install.
- After that you can launch Camel in standalone mode or deploy it in Fuse ESB (more info in README file of the project created)