.PHONY: build all clean
.DEFAULT_GOAL := all

all: build
build:
	sbt pack
