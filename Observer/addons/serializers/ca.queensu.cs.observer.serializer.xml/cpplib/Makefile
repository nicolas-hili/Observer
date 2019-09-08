CC=g++
OBSERVERLIB=../../../../ca.queensu.cs.observer.cpp/
CFLAGS=-W -Wall -ansi -pedantic -Iinclude -I$(OBSERVERLIB)/include/
LDFLAGS=-L/usr/lib/x86_64-linux-gnu/
EXEC=XmlTest
SRC=tinyxml2.cc XmlTest.cc SerializerImpl.cc
OBJ := $(addprefix obj/, $(SRC:.cc=.o))
OBJ+=$(wildcard $(OBSERVERLIB)/obj/*.o)
OBJ := $(filter-out $(OBSERVERLIB)/obj/ObserverTest.o, $(OBJ))

all: $(EXEC)

$(EXEC): $(OBJ)
	$(CC) -o $@ $^ $(LDFLAGS)

obj/%.o: src/%.cc
	$(CC) -o $@ -c $< $(CFLAGS)

obj/SerializerImpl.o: src/SerializerImpl.cc include/SerializerImpl.hh
obj/tinyxml2.o: src/tinyxml2.cc include/tinyxml2.h

.PHONY: clean mrproper

clean:
	rm -rf obj/*.o

mrproper: clean
	rm -rf $(EXEC)
