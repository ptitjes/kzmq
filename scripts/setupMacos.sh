#!/usr/bin/env bash

#
# Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
# Use of this source code is governed by the Apache 2.0 license.
#

sudo -v
xcode-select --install
brew install zeromq

pkg-config --cflags libzmq
pkg-config --libs libzmq
