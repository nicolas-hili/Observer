#include "MethodImpl.hh" 
#include "Event.hh"

int main (void) {
  MethodImpl method;
  method.connect();

  while (true) {
		std::string event = method.readData();
		if (event.length() > 2){
			printf("%s\n", event.c_str());
		}
  }

  return 1;
}
