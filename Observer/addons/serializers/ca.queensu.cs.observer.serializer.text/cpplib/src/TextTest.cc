#include "SerializerImpl.hh"
#include "Event.hh"
#include <iostream>
#include <string.h>

int main (void) {

  // Instantiate the serializer
  SerializerImpl serializer;

  // Test the serializer
  Event eventIn, eventOut;
  std::string strIn, strOut;

  // Add parameters
  eventOut.setParam("param1", "test");
  eventOut.setParam("param2", "0");

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
