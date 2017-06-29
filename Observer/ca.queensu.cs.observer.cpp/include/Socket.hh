/*
 * Socket.hh
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
 */

#ifndef SOCKET_HH_
#define SOCKET_HH_

#include <arpa/inet.h>
#include "Method.hh"
#include <netinet/in.h>
#include <sys/select.h>
#include <map>
#include <string>

class Socket: public Method {

private:
	int port;
	std::string address;

	fd_set master;    // master file descriptor list
	fd_set read_fds;  // temp file descriptor list for select()
	int fdmax;        // maximum file descriptor number

	int listener;     // listening socket descriptor
	struct sockaddr_storage remoteaddr; // client address
	socklen_t addrlen;

	char remoteIP[INET6_ADDRSTRLEN];

	struct timeval tv;

public:
	Socket();
	virtual ~Socket();
	virtual void configure(std::map<std::string, std::string> configList);
	virtual const bool canConnect() const;
	virtual int connect();
	virtual void disconnect();
	virtual std::string read();
	virtual void sendData(std::string data);

	const int getPort() const;
	void setPort(const int port);

	const std::string getAddress() const;
	void setAddress(const std::string address);
};

#endif /* SOCKET_HH_ */
