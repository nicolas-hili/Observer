//============================================================================
// Name        : RealTimeLibs.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include "event.hh"
#include "SharedMem.h"
using namespace std;

int main3() {
	cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!
	//define event class and assign the values
	events::Event e1;
	e1.setCapsuleInstance("testCapsule");
	e1.setTimePointToNow();
	e1.setEventSourceKind(events::SIGNALLING);
	e1.setEventType(events::SENDSIGNAL);
	e1.setSourceName("sampleSignal");
	e1.setPayloadField("signalPorotocol", "protocol1");
	e1.setPayloadField("param1", "paramData");
	e1.setCpuTik();
	std::cout<<e1.currentCpuTik()/CLOCKS_PER_SEC<<"\n";
	// define stream and seralize the event
	std::stringstream st;
	std::stringstream sb;
	std::stringstream sx;
	boost::archive::text_oarchive oa(st);
	boost::archive::text_oarchive bb(sb);
	boost::archive::xml_oarchive xx(sx);
	oa << e1;
    bb << e1;
    xx << BOOST_SERIALIZATION_NVP(e1);
	//// convert the seralized stream to string
	std::string ts=st.str();
	st.seekg(0, ios::end);
	int size = st.tellg();
	std::cout<<"text serialize format is:"<<st.str().length()<<"\n";
	std::cout<<" Length is"<<size<<"\n";
	std::cout<<"xml serialize format is:\n"<<sx.str()<<"\n";
	/// you can write the string to shared memory
    Comms::SharedMem shm1("test", "testq", 8900, true);
    shm1.setUp(server);
    shm1.safePushBackString(ts);
	/// read the data from shared memory as string, load the data to string stream and then deseralize,
	//you have an event object do whatever you need
    std::string ts1=shm1.safePopBackString();
	std::stringstream ss1;
	ss1<<ts1;
	boost::archive::text_iarchive ia(ss1);
    events::Event e2;
    events::Event e3;
    events::Event e4;
    std::cout<<e2<<"\n"<<e3<<"\n"<<e4<<"\n";
    ia>>e2;
    std::cout<<"new object loaded with deserialization\n" << e2<<"\n";
    std::cout<<e1.currentCpuTik()/CLOCKS_PER_SEC<<"\n";
    //e1.dumpToString("",'@');
    std::cout<<e1.dumpToString("eventid@capsuleinstance@eventsourcesind@eventpayload",';');
    //e1.dumpToString("",'@');
	return 0;
}
int main12() {
	cout << "!!!Sample Application for read event and send command!!!" << endl; // prints !!!Hello World!!!
	//define event class and assign the values
	events::Event e1;

    Comms::SharedMem eventShm("EventArea", "EventQ", 999999999, true);
    eventShm.setUp(client);
    Comms::SharedMem commandShm("CommandArea", "CommandQ", 999999999, true);
    eventShm.setUp(client);
    commandShm.setUp(client);
    while (true)
    {
    		std::string tempStr=eventShm.safePopBackString();
    		if (tempStr!="")
    		{
    			std::stringstream ts1;
    			ts1<<tempStr;
    			boost::archive::text_iarchive ia(ts1);
    		    ia>>e1;
    		    std::cout<<"new event is recieved and deserailized to event object with these fields:"<<e1<<"\n";
    		}
    		else
    		{
    			std::string tempsStr;
    			std::cout<<"enter the capsule instance to receive command, currently we send default command only\n";
    			std::cin>>tempStr;
    			//std::cout<<tempStr;
    			//std::cin>>tempStr;
    			commandShm.safePushBackString(tempStr);
    		}
    }

	return 0;
}

