/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Karim Jahed <jahed@cs.queensu.ca>
 ******************************************************************************/

#include "MethodImpl.hh"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sstream>
#include <math.h>
#include <iterator>
#include <regex>

std::queue<std::string> MethodImpl::inQueue;

char * MethodImpl::generateClientID ( const int len ) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    srand((time_t)ts.tv_nsec);

	char* s = (char*) malloc(sizeof(char) * len);
    if(s == NULL)
        return NULL;

    static const char alphanum[] =
        "0123456789"
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "abcdefghijklmnopqrstuvwxyz";

    for (int i = 0; i < len; ++i) {
        s[i] = alphanum[rand() % (sizeof(alphanum) - 1)];
    }

    s[len] = 0;
	return s;
}


int MethodImpl::messageReceived ( void * context, char * topicName, int topicLen, MQTTClient_message * message )
{
	std::string msgStr((const char*)message->payload);
	inQueue.push(msgStr);
	MQTTClient_free(topicName);
	MQTTClient_freeMessage(&message);
	return 1;
}

MethodImpl::MethodImpl() :
		Method() {
	this->client = NULL;
	this->setPort("1883");
	this->setAddress("mqtt.jahed.ca");
	this->setUsername("");
	this->setPassword("");
	this->setPubTopic("observer_out");
	this->setSubTopic("observer_in");
}

MethodImpl::~MethodImpl() {
}

void MethodImpl::configure(std::map<std::string, std::string> configList) {

	std::string port, address, username, password, subTopic, pubTopic;
	port = this->getConfig(configList, "mqtt.port");
	address = this->getConfig(configList, "mqtt.address");
	username = this->getConfig(configList, "mqtt.username");
	password = this->getConfig(configList, "mqtt.password");
	subTopic = this->getConfig(configList, "mqtt.subTopic");
	pubTopic = this->getConfig(configList, "mqtt.pubTopic");

	if (!port.empty())
		this->setPort(port);

	if (!address.empty())
		this->setAddress(address);

	if (!username.empty())
		this->setUsername(username);

	if (!password.empty())
		this->setPassword(password);

	if (!subTopic.empty())
		this->setSubTopic(subTopic);

	if (!pubTopic.empty())
		this->setPubTopic(pubTopic);
}

bool MethodImpl::canConnect() const {
	return true;
}

int MethodImpl::connect () {
	std::string uri("tcp://");
	uri.append(address);
	uri.append(":");
	uri.append(port);

	printf("MQTT uri: %s\n", uri.c_str());

	client = new MQTTClient;
	MQTTClient_create(client, uri.c_str(), generateClientID(16), MQTTCLIENT_PERSISTENCE_NONE, NULL);
	MQTTClient_setCallbacks(*client, NULL , NULL, messageReceived, NULL);

	MQTTClient_connectOptions connOpts = MQTTClient_connectOptions_initializer;
	connOpts.keepAliveInterval = 20;
	connOpts.cleansession = 1;

	if(!username.empty() && !password.empty()) {
		connOpts.username = username.c_str();
		connOpts.password = password.c_str();
	}

	int ret = MQTTClient_connect(*client, &connOpts);
	ret &= MQTTClient_subscribe(*client, subTopic.c_str(), 0);
	return ret == MQTTCLIENT_SUCCESS;
}

void MethodImpl::disconnect()
{
	if( client != NULL ) {
		MQTTClient_disconnect(*client, 1000);
		MQTTClient_destroy(client);
		client = NULL;
	}
}

std::string MethodImpl::readData() {
	if(!inQueue.empty()) {
		std::string msg = inQueue.front();
		inQueue.pop();
		return msg;
	}

	return "";
}

std::string MethodImpl::getTopic(std::string data) {
	//[FIXME] support different data pattern
	static std::regex splitter("\\|");
	std::vector<std::string> tokens;

	std::copy( std::sregex_token_iterator(data.begin(), data.end(), splitter, -1),
			  std::sregex_token_iterator(),
			  std::back_inserter(tokens));

	std::string stateName = tokens[1];
	std::string capsuleFQN = tokens[2];
	std::replace(capsuleFQN.begin(), capsuleFQN.end(), '.', '/');
	return pubTopic + std::string("/") + capsuleFQN + std::string("/") + stateName;
}

void MethodImpl::sendData(std::string data) {
	if(client != NULL)
		MQTTClient_publish(*client, getTopic(data).c_str(), data.length(), (void*) data.c_str(), 0, 0, NULL);
}

const std::string MethodImpl::getPort() const {
	return this->port;
}

void MethodImpl::setPort(const std::string port) {
	this->port = port;
}

const std::string MethodImpl::getAddress() const {
	return this->address;
}

void MethodImpl::setAddress(const std::string address) {
	this->address = address;
}

const std::string MethodImpl::getUsername() const {
	return this->username;
}

void MethodImpl::setUsername(const std::string username) {
	this->username = username;
}

const std::string MethodImpl::getPassword() const {
	return this->password;
}

void MethodImpl::setPassword(const std::string password) {
	this->password = password;
}

const std::string MethodImpl::getPubTopic() const {
	return this->pubTopic;
}

void MethodImpl::setPubTopic(const std::string pubTopic) {
	this->pubTopic = pubTopic;
}

const std::string MethodImpl::getSubTopic() const {
	return this->subTopic;
}

void MethodImpl::setSubTopic(const std::string subTopic) {
	this->subTopic = subTopic;
}
