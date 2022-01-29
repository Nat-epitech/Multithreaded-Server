JAVAC=javac
sources = $(wildcard *.java)
classes = $(sources:.java=.class)

all: program

client: program
		java Client

server: program
		java Server

program: $(classes)

fclean:
	rm -f *.class

%.class: %.java
	$(JAVAC) $<

jar: $(classes)
	jar cvf program.jar $(classes)

.PHONY: all program clean jar