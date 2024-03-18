.PHONY: build all clean
.DEFAULT_GOAL := all

all: build
build:
	scala-cli --power package -f -o teacup teacup.scala.sc
