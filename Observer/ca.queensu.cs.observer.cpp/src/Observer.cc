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

#include "Observer.hh"
#include "Config.hh"
#include "Method.hh"
#include "Serializer.hh"
#include "Event.hh"
#include <stdio.h>

Observer::Observer() {
  this->method = NULL;
  this->serializer = NULL;
}

Observer::~Observer() {
  if (this->method != NULL)
    free(this->method);
  
  if (this->serializer != NULL)
    free(this->serializer);
}

void Observer::setMethod(Method * method) {
  this->method = method;
}

void Observer::setSerializer(Serializer * serializer) {
  this->serializer = serializer;
}

int Observer::connect() {
  return this->method->connect();
}

bool Observer::canConnect() {
  return this->method->canConnect();
}

void Observer::disconnect() {
  this->method->disconnect();
}

void Observer::configure() {
  printf("number of entries: %i\n", config.load());
  this->method->configure(config.getConfigList());
  this->serializer->configure(config.getConfigList());
}

std::string Observer::read() {
  return this->method->read();
}

void Observer::sendData(const Event event) const {
  std::string strOut = this->serializer->serialize(event);
  this->method->sendData(strOut);
}

void Observer::sendData(const std::string data) const {
  this->method->sendData(data);
}

const std::string Observer::serialize(const Event event) const {
  return this->serializer->serialize(event);
}

Event Observer::parse(const std::string data) const {
  return this->serializer->parse(data);
}

