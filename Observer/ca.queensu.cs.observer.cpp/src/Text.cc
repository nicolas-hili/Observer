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

#include "Text.hh"

#include "Event.hh"

#include <string.h>
#include <sstream>
#include <iostream>
#include <sstream>
#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include <algorithm>

Text::Text() :
		Serializer() {
	this->setSeparator('|');
	this->setParamSeparator(';');
	this->setKeyValueSeparator(':');
	this->setFormat(
			"eventId|sourceName|capsuleInstance|eventSource|eventKind|seconds|nanoseconds|params");
}

Text::~Text() {
}

void Text::configure(std::map<std::string, std::string> configList) {
	std::string separator, paramSeparator, keyValueSeparator, format;

	separator = this->getConfig(configList, "text.separator");
	paramSeparator = this->getConfig(configList, "text.paramSeparator");
	keyValueSeparator = this->getConfig(configList, "text.keyValueSeparator");
	format = this->getConfig(configList, "text.format");

	if (!separator.empty())
		this->setSeparator(separator[0]);

	if (!paramSeparator.empty())
		this->setParamSeparator(paramSeparator[0]);

	if (!keyValueSeparator.empty())
		this->setKeyValueSeparator(keyValueSeparator[0]);

	if (!format.empty())
		this->setFormat(format);

}

void Text::setFormat(const std::string format) {
	this->format = format;
	std::vector<std::string> v = this->split(format, this->separator);

	if (!v.size())
		return;

	this->fields.clear();
	this->fields = v;
	this->fieldNumber = this->fields.size();
}

const std::string Text::getFormat() const {
	return this->format;
}

void Text::setSeparator(const char separator) {
	this->separator = separator;
}

void Text::setParamSeparator(const char paramSeparator) {
	this->paramSeparator = paramSeparator;
}

void Text::setKeyValueSeparator(const char keyValueSeparator) {
	this->keyValueSeparator = keyValueSeparator;
}

const std::string Text::serialize(Event event) const {

	char fieldSeparator = this->separator;

// results
	std::stringstream out;

	std::vector<std::string>::iterator it1;
	std::vector<std::string> v = this->fields;

	for (it1 = v.begin(); it1 != v.end(); ++it1) {
		std::string field = *it1;
		std::transform(field.begin(), field.end(), field.begin(), ::tolower);
		out << this->getField(field, event) << fieldSeparator;
	}

	return out.str();
}

const std::string Text::serializeParams(Event event) const {

// results
	std::stringstream out;

// params
	std::map<std::string, std::string>::const_iterator it2;
	std::map<std::string, std::string> params = event.getParams();

	if (params.empty())
		return out.str();

	it2 = params.begin();
	out << it2->first << this->keyValueSeparator << it2->second;
	++it2;

	for (; it2 != params.end(); ++it2) {
		out << this->paramSeparator << it2->first << this->keyValueSeparator
				<< it2->second;
	}

	return out.str();
}

Event Text::parse(const std::string data) const {

	char fieldSeparator = this->separator;

	std::vector<std::string> values = this->split(data, fieldSeparator);
	Event event;

	if (values.size() < fieldNumber) {
		std::cout << "Error in parsing event stream, only " << values.size()
				<< " are parsed \n";
		return event;
	}

	std::vector<std::string>::iterator it;
	std::vector<std::string> v = this->fields;
	int i = 0;

	for (it = v.begin(); it != v.end(); ++it) {
		std::string field = *it;
		std::transform(field.begin(), field.end(), field.begin(), ::tolower);
		this->setField(field, values[i], event);
		i++;
	}

	return event;
}

void Text::parseParameters(Event& event, const std::string data) const {

	std::vector<std::string> v = this->split(data, this->paramSeparator);
	std::string temp;

	if (v.size() == 0)
		return;

	event.clearParams();

	for (unsigned i = 0; i < v.size(); i++) {

		std::string::size_type sepIndex = 0;
		std::string key, value;

		temp = v[i];

		sepIndex = temp.find(this->keyValueSeparator, 0);
		key = temp.substr(0, sepIndex);
		value = temp.substr(sepIndex + 1, temp.length());

		event.setParam(key, value);

	}
}

const std::vector<std::string> Text::split(const std::string data,
		const char separator) const {

	std::vector<std::string> v;
	std::string temp = "";
	bool newField = false;

	for (unsigned int i = 0; i < data.length(); i++) {
		if (data[i] == separator) {
			newField = false;
			v.push_back(temp);
			temp = "";
		} else {
			temp = temp + (data[i]);
			newField = true;
		}
	}
	if (newField)
		v.push_back(temp);

	return v;
}

// The functions below have to be overriden when extending the observer
const std::string Text::getField(const std::string field,
		const Event& event) const {

	std::stringstream ss; // for conversion purpose

	if (field == "eventid")
		return event.getEventId();
	else if (field == "sourcename")
		return event.getSourceName();
	else if (field == "capsuleinstance")
		return event.getCapsuleInstance();
	else if (field == "eventsource") {
		ss << event.getEventSource();
		return ss.str();
	} else if (field == "eventkind") {
		ss << event.getEventKind();
		return ss.str();
	} else if (field == "seconds") {
		ss << event.getSeconds();
		return ss.str();
	} else if (field == "nanoseconds") {
		ss << event.getNanoseconds();
		return ss.str();
	} else if (field == "params") {
		return this->serializeParams(event);
	}

	return "";
}

void Text::setField(const std::string field, const std::string value,
		Event& event) const {

	std::stringstream ss; // for conversion purpose

	if (field == "eventid")
		event.setEventId(value);
	else if (field == "sourcename")
		event.setSourceName(value);
	else if (field == "capsuleinstance")
		event.setCapsuleInstance(value);
	else if (field == "eventsource") {
		event.setEventSource((Event::EventSource) (atoi(value.c_str())));
	} else if (field == "eventkind") {
		event.setEventKind((Event::EventKind) (atoi(value.c_str())));
	} else if (field == "seconds") {
		event.setSeconds(atol(value.c_str()));
	} else if (field == "nanoseconds") {
		event.setNanoseconds(atol(value.c_str()));
	} else if (field == "params") {
		this->parseParameters(event, value);
	}
}
