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

#include "SerializerImpl.hh"

#include "Event.hh"

#include <string.h>
#include <sstream>
#include <iostream>
#include <sstream>
#include <stdio.h>
#include <stdlib.h>
#include <vector>
#include <algorithm>
#include "tinyxml2.h"

SerializerImpl::SerializerImpl() :
		Serializer() {
	this->setSeparator('|');
  this->insertDeclaration(false);
	this->setFormat(
			"eventId|sourceName|capsuleInstance|eventSource|eventKind|seconds|nanoseconds|cpuTick|params");
}

SerializerImpl::~SerializerImpl() {
}

void SerializerImpl::configure(std::map<std::string, std::string> configList) {
	std::string separator, format, declaration;

	separator = this->getConfig(configList, "xml.separator");
	format = this->getConfig(configList, "xml.format");
	declaration = this->getConfig(configList, "xml.insertDeclaration");

	if (!separator.empty())
		this->setSeparator(separator[0]);

	if (!format.empty())
		this->setFormat(format);

	if (!declaration.empty() && declaration != "false")
		this->insertDeclaration(true);

}

void SerializerImpl::setFormat(const std::string format) {
	this->format = format;
	std::vector<std::string> v = this->split(format, this->separator);

	if (!v.size())
		return;

	this->fields.clear();
	this->fields = v;
	this->fieldNumber = this->fields.size();
}

void SerializerImpl::insertDeclaration(const bool value) {
	this->declaration = value;
}

const std::string SerializerImpl::getFormat() const {
	return this->format;
}

void SerializerImpl::setSeparator(const char separator) {
	this->separator = separator;
}

const std::string SerializerImpl::serialize(Event event) const {

  // results
  tinyxml2::XMLDocument doc;
  if (this->declaration) {
    doc.InsertEndChild(doc.NewDeclaration());
  }
  tinyxml2::XMLElement* root = doc.NewElement("event");
  doc.InsertEndChild(root);
  tinyxml2::XMLPrinter printer;

	std::vector<std::string>::iterator it1;
	std::vector<std::string> v = this->fields;

	for (it1 = v.begin(); it1 != v.end(); ++it1) {

		std::string field = *it1;
		std::transform(field.begin(), field.end(), field.begin(), ::tolower);

    if (field == "params") {
      tinyxml2::XMLElement* paramsField = doc.NewElement("params");

      std::map<std::string, std::string> params = event.getParams();
      std::map<std::string, std::string>::iterator it2;
      for (it2 = params.begin(); it2 != params.end(); ++it2) {
        std::string paramField = it2->first;
        tinyxml2::XMLElement* fieldName = doc.NewElement(paramField.c_str());
        tinyxml2::XMLText* value = doc.NewText(it2->second.c_str());
        fieldName->InsertEndChild(value);
        paramsField->InsertEndChild(fieldName);
      }
      root->InsertEndChild(paramsField);

      continue;
    }

    tinyxml2::XMLElement* fieldName = doc.NewElement(field.c_str());
    tinyxml2::XMLText* value = doc.NewText(event.getField(field).c_str());
    fieldName->InsertEndChild(value);
    root->InsertEndChild(fieldName);
	}

  doc.Print( &printer );
  return printer.CStr();
}

Event SerializerImpl::parse(const std::string data) const {

  const char * str = data.c_str();
  tinyxml2::XMLDocument doc;
  doc.Parse(str);

  tinyxml2::XMLElement* root = doc.FirstChildElement("event");

	Event event;

	std::vector<std::string>::iterator it;
	std::vector<std::string> v = this->fields;
	int i = 0;

	for (it = v.begin(); it != v.end(); ++it) {
		std::string field = *it;
		std::transform(field.begin(), field.end(), field.begin(), ::tolower);

    if (field == "params") {
      tinyxml2::XMLElement* params = root->FirstChildElement("params");

      for(tinyxml2::XMLElement* el = params->FirstChildElement(); el != NULL; el = el->NextSiblingElement()) {
        std::string value = el->GetText();
        const char* tagName = el->Value();
        event.setParam(tagName, value);
      }

      continue;
    }

    tinyxml2::XMLElement* fieldName = root->FirstChildElement(field.c_str());

    if (fieldName->GetText() == NULL) {
      event.setField(field, "");
    }
    else {
		  event.setField(field, fieldName->GetText());
    }

		i++;
	}

	return event;
}

const std::vector<std::string> SerializerImpl::split(const std::string data,
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
