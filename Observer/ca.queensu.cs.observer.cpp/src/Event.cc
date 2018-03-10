<<<<<<< HEAD
/*
 * Event.cpp
 *
 *  Created on: Sep 2, 2016
 *      Author: mojtababagherzadeh
 */

#include "event.hh"

namespace events {
const   std::string Event::EVENTSOURCEKINDLABEL[]={"SIGNALLING","METHOD","ACTIONECODE","TRANISTION","STATE","CAPSULE","ATTRIBUTE","TIMER","RESOURCE","CONNECTION","UNKOWNSOURCEKIND"};
const   std::string Event::EVENTTYPELABEL[]={
		"SENDSIGNAL","RECIEVESIGNAL","DEFERSIGNAL","RECALLSIGNAL","CANCELLSIGNAL", // signal event
		"METHODCALL","METHODCALLRECIEVE","METHODSTARTEXECUTE","METHODRETURN","METHODFAILED","METHODRETURNRECIEVED", // method event
		"ACTIONSTART","ACTIONEND", // action code events
		"TRANISTIONSTART","TRANISTIONEND" ,// TRANSITION
		"STATEENTRYSTART","STATEENTRYEND","STATEEXITSTART","STATEEXITEND","STATEIDLESTART","STATEIDLEEND", // state events
		"CAPSULEINSTNSIATE","CAPSULEFREE", // capsule event
		"ATTRIBUTEINSTNSIATE","ATTRIBUTEFREE","ATTRIBUTECHANGE", // attribute event
		"TIMERSTART","TIMERRESET","TIMERCANCELL","TIMERTIMEDOUT", // Timer events
		"RESOURCEASSIGNED","RESOURCERELEASED","RESOURCEPREEMPTED","RESOURCERESUMED", // resource event
		"CONNECTIONESTABLISHED","CONNECTIONFAILED", //
		"UNKOWNTYPE"
};

Event::Event() {

	this->capsuleInstance="";
	this->eventType=UNKOWNTYPE;
	this->eventSourceKind=UNKOWNSOURCEKIND;
	this->sourceName="";
	this->timePointSecond=0;
	this->timePointNano=0;
	this->eventID=generateEventID();
}

Event::~Event() {
	// TODO Auto-generated destructor stub
}

std::ostream & operator << (std::ostream & out, const Event  event )
{
	 // print all fields seperated with ';', subfiled seperated with ',' and : for mapped value
	 out<<event.getEventId()<<';';
	 out<<event.getCapsuleInstance()<<';';
	 out<<event.getEventSourceKindLabel()<<';';
	 out<<event.getEventTypeLabel()<<';';
	 out<<event.getSourceName()<<';';
	 out<<event.getCpuTik()<<';';
	 out<<event.getTimePointSecond()<<',';
	 out<<event.getTimePointNano()<<';';
	 /// message payload
	 for(std::map<std::string,std::string>::const_iterator it = event.eventPayload.begin(); it != event.eventPayload.end(); ++it)
	    	out<< it->first <<':'<<it->second<<",";
	 return out;
}
/*
std::istream & operator >> (std::istream & in, const Event  Event )
{
	return in;
}*/
const std::string& Event::getCapsuleInstance() const {
	return capsuleInstance;
}

void Event::setCapsuleInstance(const std::string& capsuleInstance) {
	this->capsuleInstance = capsuleInstance;
}

const EventPayload& Event::getEventPayload() const {
	return eventPayload;
}

void Event::setEventPayload(const EventPayload& eventPayload) {
	this->eventPayload = eventPayload;
}

EVENTSOURCEKIND Event::getEventSourceKind() const {
	return eventSourceKind;
}

void Event::setEventSourceKind(EVENTSOURCEKIND eventSourceKind) {
	this->eventSourceKind = eventSourceKind;
}


const std::string& Event::getSourceName() const {
	return sourceName;
}

void Event::setSourceName(const std::string& sourceName) {
	this->sourceName = sourceName;
}



Event::Event( std::string capsuleInstance,
		std::string sourceName) {
	this->capsuleInstance=capsuleInstance;
	this->sourceName=sourceName;
	this->eventType=UNKOWNTYPE;
	this->eventSourceKind=UNKOWNSOURCEKIND;
	this->timePointSecond=0;
	this->timePointNano=0;
	this->eventID=generateEventID();
}


void Event::setTimePointToNow() {
	UMLRTTimespec ts;
	ts.getclock(ts);
    this->setTimePointNano(ts.tv_nsec);
    this->setTimePointSecond(ts.tv_sec);
}

long Event::getTimePointNano() const {
	return timePointNano;
}

void Event::setTimePointNano(long timePointNano) {
	this->timePointNano = timePointNano;
}

long Event::getTimePointSecond() const {
	return timePointSecond;
}

Event::Event(std::string capsuleInstance,
		std::string sourceName, EVENTSOURCEKIND eventSourceKind, EVENTTYPE eventType) {
	this->capsuleInstance=capsuleInstance;
	this->sourceName=sourceName;
	this->eventSourceKind=eventSourceKind;
	this->eventType=eventType;
	this->timePointSecond=0;
	this->timePointNano=0;
	this->eventID=generateEventID();
}



Event::Event( std::string capsuleInstance,
		std::string sourceName, EVENTSOURCEKIND eventSourceKind, EVENTTYPE eventType,
		long timepointsec, long timepointnano) {
	this->capsuleInstance=capsuleInstance;
	this->sourceName=sourceName;
	this->eventSourceKind=eventSourceKind;
	this->eventType=eventType;
	this->timePointSecond=timepointsec;
	this->timePointNano=timepointnano;
	this->eventID=generateEventID();
}

Event::Event(std::string capsuleInstance,std::string sourceName,EVENTSOURCEKIND eventSourceKind,
	EVENTTYPE eventType,EventPayload eventPayload,long timepointsec,long timepointnano) {
	this->capsuleInstance=capsuleInstance;
	this->sourceName=sourceName;
	this->eventSourceKind=eventSourceKind;
	this->eventType=eventType;
	this->timePointSecond=timepointsec;
	this->timePointNano=timepointnano;
	this->eventPayload=eventPayload;
	this->eventID=generateEventID();
}

Event::Event(std::string capsuleInstance, std::string sourceName,
		EVENTSOURCEKIND eventSourceKind) {
	this->capsuleInstance=capsuleInstance;
	this->sourceName=sourceName;
	this->eventSourceKind=eventSourceKind;
	this->eventType=UNKOWNTYPE;
	this->timePointSecond=0;
	this->timePointNano=0;
	this->eventID=generateEventID();
}

void Event::setTimePointSecond(long timePointSecond) {
	this->timePointSecond = timePointSecond;
}



EVENTTYPE Event::getEventType() const {
	return eventType;
}

std::string Event::getEventSourceKindLabel(
		) const {
	return this->EVENTSOURCEKINDLABEL[this->getEventSourceKind()];
}

void Event::setEventType(EVENTTYPE eventType) {
	this->eventType = eventType;
}



std::string Event::getEventTypeLabel() const {
	return this->EVENTTYPELABEL[this->getEventType()];
}



void events::Event::setPayloadField(std::string key, std::string value) {
	if (key.length()>=1)
		this->eventPayload[key]=value;
}

 std::string events::Event::getPayloadField(std::string key) {
	if (this->eventPayload.count(key))
			return this->eventPayload.at(key);
		else
			return "";
}

std::string Event::generateEventID() {
	UMLRTTimespec ts;
	ts.getclock(ts);
	std::stringstream ss;
	ss<<ts.tv_sec<<ts.tv_nsec;
	return ss.str();
}



double events::Event::getCpuTik() const {
	return this->cpuTik;
}

const std::string& events::Event::getEventId() const {
	return eventID;
}

void events::Event::setCpuTik() {

    this->cpuTik = currentCpuTik();
}

double events::Event::currentCpuTik() {
    return double(clock());

}

std::string Event::dumpToString(std::string format, char seperator) {
	//std::vector<std::string> fieldNames;
	// parse the format was requested
	std::stringstream ss(format);
	std::string tempS;
	char ch1;
	if (format!="")
	{
		while (ss >> ch1)
	    {
			if ( ch1=='\f' || ch1=='\t' || ch1=='\v' )
				continue;
			else if (ch1=='@')
			{
				fieldNames.push_back(tempS);
				tempS="";
				continue;
			}
			else
				tempS=tempS+ch1;
	    }
		if (tempS!="")
			fieldNames.push_back(tempS);
	}
	std::stringstream ss1;
	std::vector<std::string>::iterator it;
	bool needSeperator=false;
	for(it=fieldNames.begin() ; it < fieldNames.end(); it++ )
		{
			std::string tempS1=(std::string(*it));
			std::transform(tempS1.begin(), tempS1.end(), tempS1.begin(), ::tolower);
			//std::cout<<tempS1<<"\n";
			if (tempS1=="eventsourcesind")
			{
				if (needSeperator)
					ss1<<seperator;
				else
					needSeperator=true;
				ss1<<this->getEventSourceKindLabel();
			}
			else if (tempS1=="eventtype")
			{
				if (needSeperator)
					ss1<<seperator;
				else
					needSeperator=true;
				ss1<<this->getEventTypeLabel();
			}
			else if (tempS1=="capsuleinstance")
			{
				if (needSeperator)
					ss1<<seperator;
				else
					needSeperator=true;
				ss1<<this->getCapsuleInstance();
			}
			else if (tempS1=="sourcename")
			{
			if (needSeperator)
				ss1<<seperator;
			else
				needSeperator=true;
			ss1<<this->getSourceName();
			}
			else if (tempS1=="timepointsecond")
			{
			if (needSeperator)
				ss1<<seperator;
			else
				needSeperator=true;
			ss1<<this->getTimePointSecond();
			}
			else if (tempS1=="timepointnano")
			{
			if (needSeperator)
				ss1<<seperator;
			else
				needSeperator=true;
			ss1<<this->getTimePointNano();
			}
			else if (tempS1=="eventid")
			{
			if (needSeperator)
				ss1<<seperator;
			else
				needSeperator=true;
			ss1<<this->getEventId();
			}
			else if (tempS1=="cputik")
			{
			if (needSeperator)
				ss1<<seperator;
			else
				needSeperator=true;
			ss1<<this->getCpuTik();
			}
			else if (tempS1=="eventpayload")
			{
			if (needSeperator)
				ss1<<seperator;
			else
				needSeperator=true;
			ss1<<this->payloadToString(',');
			}
	    }
	return ss1.str();
}

std::string Event::payloadToString(char seperator) {
	std::stringstream ss;
	bool needSeperator;
	for(std::map<std::string,std::string>::const_iterator it = this->eventPayload.begin(); it != this->eventPayload.end(); ++it)
	{
		if (needSeperator)
			ss<<seperator;
		else
			needSeperator=true;
		ss<< it->first <<':'<<it->second;
	}
	 return ss.str();
}

} /* namespace events */
=======
/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mojtaba Bagherzadeh <mojtaba@cs.queensu.ca>
 *     Nicolas Hili <hili@cs.queensu.ca>
 ******************************************************************************/

#include "Event.hh"

#include <string>
#include <sstream>
#include <time.h>
#include <sys/time.h>
#include <stdio.h>
#include <stdlib.h>

Event::Event(std::string capsuleInstance, std::string sourceName,
		EventSource eventSource, EventKind eventKind, long seconds,
		long nanoseconds) {

	this->generateEventId();
	this->setCapsuleInstance(capsuleInstance);
	this->setSourceName(sourceName);
	this->setEventSource(eventSource);
	this->setEventKind(eventKind);
}

const std::string Event::getCapsuleInstance() const {
	return this->capsuleInstance;
}

void Event::setCapsuleInstance(const std::string capsuleInstance) {
	this->capsuleInstance = capsuleInstance;
}

const std::string Event::getSourceName() const {
	return this->sourceName;
}

void Event::setSourceName(const std::string sourceName) {
	this->sourceName = sourceName;
}

const Event::EventSource Event::getEventSource() const {
	return this->eventSource;
}

void Event::setEventSource(const Event::EventSource source) {
	this->eventSource = source;
}

const Event::EventKind Event::getEventKind() const {
	return this->eventKind;
}

void Event::setEventKind(const Event::EventKind kind) {
	this->eventKind = kind;
}

const long Event::getSeconds() const {
	return this->seconds;
}

const long Event::getNanoseconds() const {
	return this->nanoseconds;
}

void Event::setTimestamp() {

	long seconds, nanoseconds;

	struct timeval tp;
	gettimeofday(&tp, NULL);

	seconds = tp.tv_sec;
	nanoseconds = tp.tv_usec * 1000;

	this->setTimestamp(seconds, nanoseconds);

}

void Event::setTimestamp(const long seconds, const long nanoseconds) {
	setSeconds(seconds);
	this->setNanoseconds(nanoseconds);
	this->seconds = seconds;
	this->nanoseconds = nanoseconds;
}

void Event::setSeconds(const long seconds) {
	this->seconds = seconds;
}
void Event::setNanoseconds(const long nanoseconds) {
	this->nanoseconds = nanoseconds;
}

const std::string Event::getEventId() const {
	return this->eventId;
}

void Event::setEventId(const std::string eventId) {
	this->eventId = eventId;
}

void Event::generateEventId() {

	long seconds, nanoseconds;

	struct timeval tp;
	gettimeofday(&tp, NULL);

	seconds = tp.tv_sec;
	nanoseconds = tp.tv_usec * 1000;

	std::stringstream ss;
	int r;
	r = random();

	ss << seconds << nanoseconds << r;
	this->eventId = ss.str();
}

const std::map<std::string, std::string> Event::getParams() const {
	return params;
}

const std::string Event::getParam(std::string key) const {
	bool n = this->params.count(key);
	return (n) ? this->params.at(key) : "";
}

void Event::setParams(const std::map<std::string, std::string> params) {
	this->params = params;
}

void Event::setParam(const std::string key, const std::string value) {
	if (key.length() >= 1)
		this->params[key] = value;
}

void Event::setParam(const std::string key, const int value) {
	std::stringstream v;
	v << value;
	this->setParam(key, v.str());
}

void Event::clearParams() {
	this->params.clear();
}
>>>>>>> r1remote/master
