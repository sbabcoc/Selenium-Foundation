#!/bin/bash
error_exit()
{
  echo "$1" 1>&2
  exit 1
}

. gradle.properties
if [[ ! "${version}" =~ -SNAPSHOT$ ]]; then
  error_exit "not snapshot version: ${version}"
fi

while getopts p: flag
do
  case "${flag}" in
    p) profile=${OPTARG};;
  esac
done
case "${profile}" in
  selenium2) seleniumApi=s2;;
  selenium3) seleniumApi=s3;;
  *) error_exit "unknown profile: ${profile}";;
esac
revision=${version/-/-${seleniumApi}-}
mvn -Drevision=${revision} clean install -P${profile}
