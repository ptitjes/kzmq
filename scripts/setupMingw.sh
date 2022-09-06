#!/usr/bin/env bash

#
# Copyright (c) 2022 Didier Villevalois and Kzmq contributors.
# Use of this source code is governed by the Apache 2.0 license.
#

pacman --noconfirm -Syu
pacman --noconfirm -S --needed mingw-w64-x86_64-zeromq

pkg-config --cflags libzmq
pkg-config --libs libzmq
