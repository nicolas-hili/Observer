/*
 * Event.hh
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
 */

#ifndef EVENT_HH_
#define EVENT_HH_

#include <map>
#include <iostream>
#include <string.h>
#include <sstream>
#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include <time.h>

class Event {

public:
  typedef enum EventSource {
		Signal,
		Method,
		ActionCode,
		Transition,
		State,
		Capsule,
		Attribute,
		Timer,
		UnknownSource,
		Command
	} EventSource;

	typedef enum EventKind {
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
		UnknownKind,
		List,		  // Command events
		Show,
		Trigger
	} EventKind;

private:
	std::string capsuleInstance;
	std::string sourceName;
	std::string eventId;
	long seconds;
	long nanoseconds;
	EventSource eventSource;
	EventKind eventKind;
	double cpuTick;
	std::map<std::string, std::string> params;

public:
	Event(std::string capsuleInstance = "", std::string sourceName = "",
			EventSource eventSource = UnknownSource, EventKind eventKind =
					UnknownKind, long seconds = 0, long nanoseconds = 0);
	const std::string getCapsuleInstance() const;
	void setCapsuleInstance(const std::string capsuleInstance);
	std::string getSourceName() const;
	void setSourceName(const std::string sourceName);
	EventSource getEventSource() const;
	void setEventSource(const Event::EventSource source);
	EventKind getEventKind() const;
	void setEventKind(const Event::EventKind kind);
	long getSeconds() const;
	long getNanoseconds() const;
	void setTimestamp();
	void setTimestamp(const long seconds, const long nanoseconds = 0);
	void setSeconds(const long seconds);
	void setNanoseconds(const long nanoseconds = 0);
	void setEventId(const std::string eventId);
	const std::string getEventId() const;
	void generateEventId();
	double getCpuTick() const;
	void setCpuTick(double tick = 0.0);
	static double currentCpuTick();
	const std::map<std::string, std::string> getParams() const;
	const std::string getParam(std::string key) const;
	void setParams(const std::map<std::string, std::string> params);
	void setParam(const std::string key, const std::string value);
	void setParam(const std::string key, const int value);
	void clearParams();
  void setField(const std::string field, const std::string value);
  const std::string getField(const std::string field) const;
};

#endif /* EVENT_HH_ */
