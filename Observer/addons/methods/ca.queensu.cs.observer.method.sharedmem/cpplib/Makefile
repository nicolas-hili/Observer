CC=g++
OBSERVERLIB=../../../../ca.queensu.cs.observer.cpp/
CFLAGS=-W -Wall -ansi -pedantic -Iinclude -I$(OBSERVERLIB)/include/ -I/usr/include/boost/
LDFLAGS=-L/usr/lib/x86_64-linux-gnu/ -lpthread  -lrt
EXEC=SharedMemTest
SRC= SharedMemTest.cc MethodImpl.cc
OBJ := $(addprefix obj/, $(SRC:.cc=.o))
OBJ+=$(wildcard $(OBSERVERLIB)/obj/*.o)
OBJ := $(filter-out $(OBSERVERLIB)/obj/ObserverTest.o, $(OBJ))

all: $(EXEC)

$(EXEC): $(OBJ)
	$(CC) -o $@ $^ $(LDFLAGS)

obj/%.o: src/%.cc
	$(CC) -o $@ -c $< $(CFLAGS)

obj/MethodImpl.o: src/MethodImpl.cc include/MethodImpl.hh

.PHONY: clean mrproper

clean:
	rm -rf obj/*.o

mrproper: clean
	rm -rf $(EXEC)
