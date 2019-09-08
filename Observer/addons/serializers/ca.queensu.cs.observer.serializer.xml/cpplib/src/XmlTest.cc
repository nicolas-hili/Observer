#include "SerializerImpl.hh"
#include "Event.hh"
#include <iostream>
#include <string.h>
#include "Config.hh"

int main (void) {

  // Get configuration
  Config config;
  printf("number of entries: %i\n", config.load());

  // Instantiate the serializer
  SerializerImpl serializer;
  serializer.configure(config.getConfigList());

  // Test the serializer
  Event eventIn, eventOut;
  std::string strIn, strOut;

  eventOut.setParam("myParam1", "test");

  // Serialize eventOut
  strOut = serializer.serialize(eventOut);
  std::cout << strOut << std::endl;

  // Parse and re-serialize eventIn
  eventIn = serializer.parse(strOut);
  strIn = serializer.serialize(eventIn);
  std::cout << strIn << std::endl;

  if (strIn == strOut)
    std::cout << "success" << std::endl;
  else
    std::cout << "failure" << std::endl;

  return 1;
}
