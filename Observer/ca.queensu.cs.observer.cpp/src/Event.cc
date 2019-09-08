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
	this->setCpuTick();
  this->setTimestamp(seconds, nanoseconds);
}

const std::string Event::getCapsuleInstance() const {
	return this->capsuleInstance;
}

void Event::setCapsuleInstance(const std::string capsuleInstance) {
	this->capsuleInstance = capsuleInstance;
}

std::string Event::getSourceName() const {
	return this->sourceName;
}

void Event::setSourceName(const std::string sourceName) {
	this->sourceName = sourceName;
}

Event::EventSource Event::getEventSource() const {
	return this->eventSource;
}

void Event::setEventSource(const Event::EventSource source) {
	this->eventSource = source;
}

Event::EventKind Event::getEventKind() const {
	return this->eventKind;
}

void Event::setEventKind(const Event::EventKind kind) {
	this->eventKind = kind;
}

long Event::getSeconds() const {
	return this->seconds;
}

long Event::getNanoseconds() const {
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

double Event::getCpuTick() const {
	return this->cpuTick;
}

void Event::setCpuTick(double tick) {
  if (tick == 0.0)
    this->cpuTick = currentCpuTick();
  else
    this->cpuTick = tick;
}

double Event::currentCpuTick() {
  return double(clock());
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

// The functions below have to be overriden when extending the fields of the observer
const std::string Event::getField(const std::string field) const {

	std::stringstream ss; // for conversion purpose

	if (field == "eventid")
		return this->getEventId();
	else if (field == "sourcename")
		return this->getSourceName();
	else if (field == "capsuleinstance")
		return this->getCapsuleInstance();
	else if (field == "eventsource") {
		ss << this->getEventSource();
		return ss.str();
	} else if (field == "eventkind") {
		ss << this->getEventKind();
		return ss.str();
	} else if (field == "seconds") {
		ss << this->getSeconds();
		return ss.str();
	} else if (field == "nanoseconds") {
		ss << this->getNanoseconds();
		return ss.str();
	} else if (field == "cputick") {
		ss << this->getCpuTick();
		return ss.str();
	}

	return "";
}

void Event::setField(const std::string field, const std::string value) {

	std::stringstream ss; // for conversion purpose

	if (field == "eventid")
		this->setEventId(value);
	else if (field == "sourcename")
		this->setSourceName(value);
	else if (field == "capsuleinstance")
		this->setCapsuleInstance(value);
	else if (field == "eventsource") {
		this->setEventSource((Event::EventSource) (atoi(value.c_str())));
	} else if (field == "eventkind") {
		this->setEventKind((Event::EventKind) (atoi(value.c_str())));
	} else if (field == "seconds") {
		this->setSeconds(atol(value.c_str()));
	} else if (field == "nanoseconds") {
		this->setNanoseconds(atol(value.c_str()));
	} else if (field == "cputick") {
		this->setCpuTick(atol(value.c_str()));
	}
}
