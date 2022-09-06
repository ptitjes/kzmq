#!/usr/bin/env bash

#
# Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
# Use of this source code is governed by the Apache 2.0 license.
#

sudo apt-get -y install libzmq3-dev

pkg-config --cflags libzmq
pkg-config --libs libzmq
