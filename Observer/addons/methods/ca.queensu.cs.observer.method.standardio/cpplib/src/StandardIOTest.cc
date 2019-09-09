#include "MethodImpl.hh" 
#include "Event.hh"
#include "Config.hh"

int main (void) {
  
  // Get configuration
  Config config;
  printf("number of entries: %i\n", config.load());

  // Instantiate the method
  MethodImpl method;
  method.configure(config.getConfigList());

  // Connect
  method.connect();

  while (true) {
		std::string strIn = method.readData();
    if (!strIn.empty()) {
      method.sendData(strIn);
    }
  }

  return 1;
}
