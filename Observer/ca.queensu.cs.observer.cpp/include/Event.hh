/*
<<<<<<< HEAD
 * Event.h
 *
 *  Created on: Sep 2, 2016
 *      Author: mojtababagherzadeh
 *      event class for saving real-time system events during observation
 *      part of the event kind and type  are defined based on paper
 *      Graf, Susanne, Ileana Ober, and Iulian Ober. "A real-time profile for UML."
 *      International Journal on Software Tools for Technology Transfer 8.2 (2006): 113-127.
=======
 * Event.hh
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
>>>>>>> r1remote/master
 */

#ifndef EVENT_HH_
#define EVENT_HH_
<<<<<<< HEAD
#include <iostream>
#include <map>
#include "umlrttimespec.hh"
#include <boost/archive/text_oarchive.hpp>
#include <boost/archive/text_iarchive.hpp>
#include <boost/archive/binary_oarchive.hpp>
#include <boost/archive/binary_iarchive.hpp>
#include <boost/archive/xml_iarchive.hpp>
#include <boost/archive/xml_oarchive.hpp>
#include <boost/serialization/map.hpp>
#include <sstream>
#include  <time.h>
#include "umlrtobjectclass.hh"

namespace events {
// main category for the event sources, refer to apendix of paper for detail
enum EVENTSOURCEKIND{SIGNALLING,METHOD,ACTIONECODE,TRANISTION,STATE,CAPSULE,ATTRIBUTE,TIMER,RESOURCE,CONNECTION,UNKOWNSOURCEKIND};
// types of signal events
enum EVENTTYPE{
	SENDSIGNAL,RECIEVESIGNAL,DEFERSIGNAL,RECALLSIGNAL,CANCELLSIGNAL, // signal event
	METHODCALL,METHODCALLRECIEVE,METHODSTARTEXECUTE,METHODRETURN,METHODFAILED,METHODRETURNRECIEVED, // method event
	ACTIONSTART,ACTIONEND, // action code events
	TRANISTIONSTART,TRANISTIONEND ,// TRANSITION
	STATEENTRYSTART,STATEENTRYEND,STATEEXITSTART,STATEEXITEND,STATEIDLESTART,STATEIDLEEND, // state events
	CAPSULEINSTNSIATE,CAPSULEFREE, // capsule event
	ATTRIBUTEINSTNSIATE,ATTRIBUTEFREE,ATTRIBUTECHANGE, // attribute event
	TIMERSTART,TIMERRESET,TIMERCANCELL,TIMERTIMEDOUT, // Timer events
	RESOURCEASSIGNED,RESOURCERELEASED,RESOURCEPREEMPTED,RESOURCERESUMED,   // resource event
	CONNECTIONESTABLISHED,CONNECTIONFAILED, // connection event
	UNKOWNTYPE
};
//enum SIGNALINGEVENTTYPE{SENDSIGNAL,RECIEVESIGNAL,DEFERSIGNAL,RECALLSIGNAL,CANCELLSIGNAL};
// types of event for function call
//enum METHODEVENTTYPE{METHODCALL,METHODCALLRECIEVE,METHODSTARTEXECUTE,METHODRETURN,METHODFAILED,METHODRETURNRECIEVED};
// types of event for action codes
//enum ACTIONCODEEVENTTYPE{ACTIONSTART,ACTIONEND};
// types of event for transitions
//enum TRAISIONEVENTTYPE{TRAISIONSTART,TRAISIONEND};
// types of event for state
//enum STATEEVENTTYPE{STATEENTRYSTART,STATEENTRYEND,STATEEXITSTART,STATEEXITEND,STATEIDLESTART,STATEIDLEEND};
// types of event for capsule
//enum CAPSULEEVENTTYPE{CAPSULEINSTNSIATE,CAPSULEFREE};
// types of event for function call
//enum ATTRIBUTEEVENTTYPE{ATTRIBUTEINSTNSIATE,ATTRIBUTEFREE,ATTRIBUTECHANGE};
// types of event for timer
//enum TIMEREVENTTYPE{TIMERSTART,TIMERRESET,TIMERCANCELL,TIMERTIMEDOUT};
// types of event for resources
//enum RESOURCEEVENTTYPE{RESOURCEASSIGNED,RESOURCERELEASED,RESOURCEPREEMPTED,RESOURCERESUMED};
/// the map is used to save the message payload for each event type and can be customized based on event type
typedef std::map<std::string,std::string> EventPayload;
class Event {
public:
	Event(std::string capsuleInstance,std::string sourceName);
	Event(std::string capsuleInstance,std::string sourceName, EVENTSOURCEKIND eventSourceKind);
	Event(std::string capsuleInstance,std::string sourceName,EVENTSOURCEKIND eventSourceKind, EVENTTYPE eventType);
	Event(std::string capsuleInstance,std::string sourceName,EVENTSOURCEKIND eventSourceKind, EVENTTYPE eventType,long timepointsec,long timepointnano=0);
	Event(std::string capsuleInstance,std::string sourceName,EVENTSOURCEKIND eventSourceKind, EVENTTYPE eventType,EventPayload eventPaylod,long timepointsec=0,long timepointnano=0);
    Event();
	virtual ~Event();
	const std::string& getCapsuleInstance() const;
	void setCapsuleInstance(const std::string& capsuleInstance);
	const EventPayload& getEventPayload() const;
	void setEventPayload(const EventPayload& eventPayload);
	EVENTSOURCEKIND getEventSourceKind() const;
	void setEventSourceKind(EVENTSOURCEKIND eventsource);
	const std::string& getSourceName() const;
	void setSourceName(const std::string& sourceName);
	/// set time of event to the current timestamp, the time stamp is the nanosecond counted from 1970 and saved as sec and nanosecond
	void setTimePointToNow();
	friend std::ostream & operator << (std::ostream & out, const Event  Event );
	//friend std::istream & operator >> (std::istream & in, const Event  Event );
	long getTimePointNano() const;
	void setTimePointNano(long timePointNano);
	long getTimePointSecond() const;  // read the current timestamp and save them in related fields, it is better we use this field instead of setting time manually
	void setTimePointSecond(long timePointSecond);
	/// add filed to the message payload
	void setPayloadField(std::string key, std::string value);
	std::string getPayloadField(std::string key);
	std::string  getEventSourceKindLabel() const;
	EVENTTYPE getEventType() const;
	void setEventType(EVENTTYPE eventType);
	std::string getEventTypeLabel() const;

    //// label for enumeration
	static const   std::string EVENTSOURCEKINDLABEL[];
	static const   std::string EVENTTYPELABEL[];

    //// generate unqiue if for each thread
	static std::string generateEventID();
    //// cpu time fuction
	double getCpuTik() const;
	void setCpuTik();  ///  this save the cpu tick process until that moment, by using setCpuTik/CLOCKS_PER_SEC can be converted to second
	static double currentCpuTik();
	/// Unique event id
	const std::string& getEventId() const;
    //// customize dump of event's fields to string

	std::string dumpToString(std::string format,  char seperator='@');
	std::string payloadToString(char seperator=',');

private:
	/// implementaion related to serialization
	friend class boost::serialization::access;
	template<class Archive> void serialize(Archive & ar, const unsigned int version=1,const std::string requiredField="");

	/// filed detail
	EVENTSOURCEKIND eventSourceKind; // shows the evenetsource kind
	EVENTTYPE eventType;                   // shows event type
	std::string capsuleInstance;     // show capsule instance name that generate event
	std::string sourceName;          // based on the event source kind, this fields shows the
	long timePointSecond;
	long timePointNano;
	EventPayload eventPayload;
	std::string eventID;
	double cpuTik;
	////for generating and parsing purpose
	std::vector<std::string> fieldNames;
	//std::map<std::string,bool> fieldsInOutput;

};

} /* namespace events */
//// function to serialize, whenever we add new data filed, if we need to send it to clinet add the related line in below function
template<class Archive>
inline void events::Event::serialize(Archive& ar, const unsigned int version,
		const std::string requiredField) {
	ar & BOOST_SERIALIZATION_NVP(eventID);
	ar & BOOST_SERIALIZATION_NVP(eventSourceKind);
	//ar & "test1";
	ar & BOOST_SERIALIZATION_NVP(eventType);
	ar & BOOST_SERIALIZATION_NVP(capsuleInstance);
	ar & BOOST_SERIALIZATION_NVP(sourceName);
	ar & BOOST_SERIALIZATION_NVP(cpuTik);
	ar & BOOST_SERIALIZATION_NVP(timePointSecond);
	ar & BOOST_SERIALIZATION_NVP(timePointNano);
	ar & BOOST_SERIALIZATION_NVP(eventPayload);

}

=======

#include <map>
#include <iostream>
#include <string.h>
#include <sstream>
#include <stdio.h>
#include <stdlib.h>
#include <vector>

class Event {

public:
typedef enum {
		Signal,
		Method,
		ActionCode,
		Transition,
		State,
		Capsule,
		Attribute,
		Timer,
		UnknownSource
	} EventSource;

	typedef enum {
		SendSignal, ReceiveSignal, DeferSignal, RecallSignal, CancelSignal, // Signal events
		MethodCall,
		MethodCallReceive,
		MethodStartExecute,
		MethodReturn,
		MethodFailed,
		MethodReturnReceived, // Method events
		ActionStart,
		ActionEnd, // Action code events
		TransitionStart,
		TransitionEnd, // Transition events
		StateEntryStart,
		StateEntryEnd,
		StateExitStart,
		StateExitEnd,
		StateIdleStart,
		StateIdleEnd, // State events
		CapsuleInstantiate,
		CapsuleFree, // Capsule events
		AttributeInstantiate,
		AttributeFree,
		AttributeChange, // Attribute events
		TimerStart,
		TimerSet,
		TimerCancel,
		TimerTimeout, // Timer events
		UnknownKind
	} EventKind;

private:
	std::string capsuleInstance;
	std::string sourceName;
	std::string eventId;
	long seconds;
	long nanoseconds;
	EventSource eventSource;
	EventKind eventKind;
	std::map<std::string, std::string> params;

public:
	Event(std::string capsuleInstance = "", std::string sourceName = "",
			EventSource eventSource = UnknownSource, EventKind eventKind =
					UnknownKind, long seconds = 0, long nanoseconds = 0);
	const std::string getCapsuleInstance() const;
	void setCapsuleInstance(const std::string capsuleInstance);
	const std::string getSourceName() const;
	void setSourceName(const std::string sourceName);
	const EventSource getEventSource() const;
	void setEventSource(const Event::EventSource source);
	const EventKind getEventKind() const;
	void setEventKind(const Event::EventKind kind);
	const long getSeconds() const;
	const long getNanoseconds() const;
	void setTimestamp();
	void setTimestamp(const long seconds, const long nanoseconds = 0);
	void setSeconds(const long seconds);
	void setNanoseconds(const long nanoseconds = 0);
	void setEventId(const std::string eventId);
	const std::string getEventId() const;
	void generateEventId();
	const std::map<std::string, std::string> getParams() const;
	const std::string getParam(std::string key) const;
	void setParams(const std::map<std::string, std::string> params);
	void setParam(const std::string key, const std::string value);
	void setParam(const std::string key, const int value);
	void clearParams();
};

>>>>>>> r1remote/master
#endif /* EVENT_HH_ */
