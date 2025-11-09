# Simple build for JavaFX app without external build tools

JFX_VERSION := 23
SRC := Main.java
MAIN := Main

.PHONY: deps build run clean

# Download JavaFX SDK into .javafx/ (architecture-aware) and output lib path
JFX_LIB := $(shell bash -lc 'chmod +x scripts/javafx-setup.sh >/dev/null 2>&1 || true; scripts/javafx-setup.sh')

deps:
	@chmod +x scripts/javafx-setup.sh
	@echo "JavaFX lib dir: $(JFX_LIB)"

build: deps
	javac --module-path "$(JFX_LIB)" --add-modules=javafx.controls,javafx.graphics $(SRC)

run: build
	java --module-path "$(JFX_LIB)" --add-modules=javafx.controls,javafx.graphics $(MAIN)

clean:
	rm -f *.class
