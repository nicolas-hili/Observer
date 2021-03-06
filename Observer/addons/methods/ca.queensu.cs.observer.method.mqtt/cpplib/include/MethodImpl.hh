/*
 * Method implementation for MQTT
 *
 *  Created on: Mar, 21, 2019
 *      Author: karim
 */

#ifndef METHOD_IMPL_HH_
#define METHOD_IMPL_HH_

#include "Method.hh"
#include <map>
#include <string>
#include <queue>
#include <MQTTClient.h>

class MethodImpl: public Method {

private:
	std::string port;
	std::string address;
	std::string username;
	std::string password;
	std::string subTopic;
	std::string pubTopic;
	MQTTClient * client;

	std::string getTopic(std::string data);

	static std::queue<std::string> inQueue;
	static char * generateClientID ( const int len );
	static int messageReceived ( void * context, char * topicName, int topicLen, MQTTClient_message * message );

public:
	MethodImpl();
	virtual ~MethodImpl();
	virtual void configure(std::map<std::string, std::string> configList);
	virtual bool canConnect() const;
	virtual int connect();
	virtual void disconnect();
	virtual std::string readData();
	virtual void sendData(std::string data);

	const std::string getPort() const;
	void setPort(const std::string port);

	const std::string getAddress() const;
	void setAddress(const std::string address);

	const std::string getUsername() const;
	void setUsername(const std::string username);

	const std::string getPassword() const;
	void setPassword(const std::string password);

	const std::string getPubTopic() const;
	void setPubTopic(const std::string pubTopic);

	const std::string getSubTopic() const;
	void setSubTopic(const std::string subTopic);

};

#endif /* METHOD_IMPL_HH_ */
