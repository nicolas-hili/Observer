#include "ObserverTest.hh"
#include "Observer.hh"
#include "Method.hh"
#include "Serializer.hh"
#include "MethodImpl.hh"
#include "SerializerImpl.hh"
#include "Event.hh"
#include <stdio.h>

int main () {

  // Instantiate the Observer
  Observer observer;

  // Instantiate the serializer
  Serializer* serializer;
  serializer = new SerializerImpl();
  observer.setSerializer(serializer);

  // Instantiate the communication method
  Method* method;
  method = new MethodImpl();
  observer.setMethod(method);

  // Configure the Observer
  observer.configure();

  // Initiate the connection through the communication method
  observer.connect();

  // testing the serializer
  Event eventIn, eventOut;
  std::string strIn, strOut;

  while (true) {

    // Read any event coming in
		strIn = method->read();

		if (strIn != "") {

      // Display the event to the console
			std::cout << "Receiving: " << strIn << std::endl;

      // Parse the event
      try {
        eventIn = observer.parse(strIn);

        // Build output event
        eventOut.setEventId(eventIn.getEventId());
        eventOut.setSourceName(eventIn.getSourceName());
        eventOut.setCapsuleInstance(eventIn.getCapsuleInstance());
        eventOut.setEventSource(eventIn.getEventSource());
        eventOut.setEventKind(eventIn.getEventKind());
        eventOut.setTimestamp(
            eventIn.getSeconds(),
            eventIn.getNanoseconds()
        );
        eventOut.setCpuTick(eventIn.getCpuTick());
        strOut = observer.serialize(eventOut);

        std::cout << "Sending back: " << strOut << std::endl;

        observer.sendData(eventOut);
        

      } catch (const std::exception& e) {
        std::cout << "Invalid format" << std::endl;
      }

		}
    strIn = "";
  }

    eventOut.setSourceName("source name");
    eventOut.setParam("key1", "value1");
//
//    eventIn.setSourceName("source name");
//    eventIn.setParam("key1", "value1");
//    eventIn.setParam("key2", "value2");
//    eventIn.setParam("key3", "value3");
    eventOut.setEventSource(Event::Command);
    eventOut.setEventKind(Event::CancelSignal);
//    strIn = serializer->serialize(eventIn);

    strOut = observer.serialize(eventOut);
    observer.sendData(strOut);

    strIn = observer.read();
    observer.sendData(observer.serialize(eventIn));
//    eventOut = serializer->parse(strIn);
//    strOut = serializer->serialize(eventOut);

//    printf("%s\n", strIn.c_str());
//    printf("%s\n", strOut.c_str());


    // Test list function
//   method->sendData(strOut);


    observer.disconnect();
    free(serializer);
    free(method);


    return 0;
}

