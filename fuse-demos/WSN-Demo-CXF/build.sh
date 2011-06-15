#!/bin/bash
mvn install
cd wsn-demo-sa
mvn jbi:projectDeploy
cd ..
