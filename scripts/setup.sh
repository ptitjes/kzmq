#!/usr/bin/env bash

#
# Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
# Use of this source code is governed by the Apache 2.0 license.
#

ROOT="$(dirname "${BASH_SOURCE[0]}")"
OS="$(uname | tr '[:upper:]' '[:lower:]')"
case $OS in
  darwin*)  echo "OSX detected" && "${ROOT}"/setupMacos.sh ;;
  linux*)   echo "Linux detected" && "${ROOT}"/setupUbuntu.sh ;;
  mingw*)   echo "Mingw detected" && "${ROOT}"/setupMingw.sh ;;
  msys*)    echo "Windows detected (unsupported)" && exit 1 ;;
  *)        echo "Unknown OS: $OS" && exit 1 ;;
esac
