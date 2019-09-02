include config.mk

SERIALIZER_PREFIX?=../addons/serializers/ca.queensu.cs.observer.serializer.
METHOD_PREFIX?=../addons/methods/ca.queensu.cs.observer.method.

SERIALIZER_CPPLIB=$(SERIALIZER_PREFIX)$(SERIALIZER)/cpplib
SERIALIZER_INCLUDE=$(SERIALIZER_CPPLIB)/include
SERIALIZER_OBJ=$(SERIALIZER_CPPLIB)/obj/SerializerImpl.o

METHOD_CPPLIB=$(METHOD_PREFIX)$(METHOD)/cpplib
METHOD_INCLUDE=$(METHOD_CPPLIB)/include
METHOD_OBJ=$(METHOD_CPPLIB)/obj/MethodImpl.o

CC=g++
ADDONS=../addons/
CFLAGS=-W -Wall -ansi -pedantic -Iinclude -I$(SERIALIZER_INCLUDE) -I$(METHOD_INCLUDE)
LDFLAGS=
EXEC=ObserverTest
SRC= ObserverTest.cc Observer.cc Config.cc Event.cc Method.cc Serializer.cc
OBJ= $(addprefix obj/, $(SRC:.cc=.o))
OBJ+=$(SERIALIZER_OBJ)
OBJ+=$(METHOD_OBJ)

all: $(EXEC)

$(EXEC): $(OBJ)
	$(CC) -o $@ $^ $(LDFLAGS)

obj/%.o: src/%.cc include/%.hh 
	$(CC) -o $@ -c $< $(CFLAGS)

.PHONY: clean mrproper

clean:
	rm -rf obj/*.o

mrproper: clean
	rm -rf $(EXEC)
