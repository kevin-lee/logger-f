#!/bin/bash -e

set -x

if [ -z "$2" ]
  then
    echo "Missing parameters. Please enter the [project] and [Scala version]."
    echo "sbt-build.sh core 2.13.4"
    exit 1
else
  : ${CURRENT_BRANCH_NAME:?"CURRENT_BRANCH_NAME is missing."}

  project_name=$1
  scala_version=$2
  echo "============================================"
  echo "Build projects"
  echo "--------------------------------------------"
  echo ""
  export SOURCE_DATE_EPOCH=$(date +%s)
  echo "SOURCE_DATE_EPOCH=$SOURCE_DATE_EPOCH"

  if [[ "$CURRENT_BRANCH_NAME" == "main" || "$CURRENT_BRANCH_NAME" == "logger-f-1" || "$CURRENT_BRANCH_NAME" == "release" ]]
  then
#    sbt -J-Xmx8G "project ${project_name}" ++${scala_version} -v clean coverage test coverageReport coverageAggregate
#    sbt -J-Xmx8G "project ${project_name}" ++${scala_version} -v coveralls
#    sbt -J-Xmx8G "project ${project_name}" ++${scala_version} -v clean packagedArtifacts
    mkdir -p dotty-docs
    sbt \
      "project ${project_name}" \
      ++${scala_version} \
      -v \
      clean \
      test \
      packagedArtifacts
  else
#    sbt -J-Xmx8G "project ${project_name}" ++${scala_version} -v clean coverage test coverageReport coverageAggregate package
#    sbt -J-Xmx8G "project ${project_name}" ++${scala_version} -v coveralls
    sbt \
      "project ${project_name}" \
      ++${scala_version} \
      -v \
      clean \
      test \
      package
  fi


  echo "============================================"
  echo "Building projects: Done"
  echo "============================================"
fi
