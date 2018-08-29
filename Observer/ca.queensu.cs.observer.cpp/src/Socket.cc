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

#include "Socket.hh"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sstream>

// get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa) {
	if (sa->sa_family == AF_INET) {
		return &(((struct sockaddr_in*) sa)->sin_addr);
	}

	return &(((struct sockaddr_in6*) sa)->sin6_addr);
}

Socket::Socket() :
		Method() {
	this->setPort(8080);
	this->setAddress("localhost");
}

Socket::~Socket() {
}

void Socket::configure(std::map<std::string, std::string> configList) {

	std::string port, address;
	port = this->getConfig(configList, "socket.port");
	address = this->getConfig(configList, "socket.address");

	if (!port.empty())
		this->setPort(atoi(port.c_str()));

	if (!address.empty())
		this->setAddress(address);

	printf("address: %s, port: %d\n", getAddress().c_str(), getPort());
}

const bool Socket::canConnect() const {
	return true;
}

int Socket::connect() {

	int rv, result = -1;
	struct addrinfo hints, *ai, *p;
	int yes = 1;   // for setsockopt

	std::stringstream port;
	port << this->getPort();

	tv.tv_sec = 0;
	tv.tv_usec = 0;

	FD_ZERO(&master);    // clear the master and temp sets
	FD_ZERO(&read_fds);

	// get us a socket and bind it
	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;
	if ((rv = getaddrinfo(NULL, port.str().c_str(), &hints, &ai)) != 0) {
		fprintf(stderr, "selectserver: %s\n", gai_strerror(rv));
		exit(1);
	}

	for (p = ai; p != NULL; p = p->ai_next) {
		listener = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
		if (listener < 0) {
			continue;
		}

		// lose the pesky "address already in use" error message
		setsockopt(listener, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

		if (bind(listener, p->ai_addr, p->ai_addrlen) < 0) {
			close(listener);
			continue;
		}

		break;
	}

		// if we got here, it means we didn't get bound
	if (p == NULL) {
		fprintf(stderr, "selectserver: failed to bind\n");
		exit(2);
	}

	freeaddrinfo(ai); // all done with this

	// listen
	if (listen(listener, 10) == -1) {
		perror("listen");
		exit(3);
	}

	// add the listener to the master set
	FD_SET(listener, &master);

	// keep track of the biggest file descriptor
	fdmax = listener; // so far, it's this one

	return result;

}

void Socket::disconnect() {
}

std::string Socket::read() {

	// main loop
	read_fds = master; // copy it
	int rc = select(fdmax + 1, &read_fds, NULL, NULL, &tv);
	if (rc == -1) {
		perror("select");
		exit(4);
	}

	tv.tv_sec = 0;
	tv.tv_usec = 0;

	if (rc == 0) {
		//   printf("timeout\n");
		return "";
	}

	int i = 0;

	// run through the existing connections looking for data to read
	for (i = 0; i <= fdmax; i++) {
		if (FD_ISSET(i, &read_fds)) { // we got one!!
			if (i == listener) {
				// handle new connections
				addrlen = sizeof remoteaddr;
				int newfd = accept(listener, (struct sockaddr *) &remoteaddr,
						&addrlen);

				char remoteIP[INET6_ADDRSTRLEN];

				if (newfd == -1) {
					perror("accept");
				} else {
					FD_SET(newfd, &master); // add to master set
					if (newfd > fdmax) {    // keep track of the max
						fdmax = newfd;
					}
					printf("new connection from %s on "
							"socket %d\n",
							inet_ntop(remoteaddr.ss_family,
									get_in_addr((struct sockaddr*) &remoteaddr),
									remoteIP, INET6_ADDRSTRLEN), newfd);
				}
			} else {
				char buf[256];    // buffer for client data
				int nbytes;
				// handle data from a client
				if ((nbytes = recv(i, buf, sizeof buf, 0)) <= 0) {
					// got error or connection closed by client
					if (nbytes == 0) {
						// connection closed
						printf("socket %d hung up\n", i);
					} else {
						perror("recv");
					}
					close(i); // bye!
					FD_CLR(i, &master); // remove from master set
				} else {
					std::string str(buf, buf + nbytes - 2);
					return str;
//    printf("new data: %s\n", buf);
//    // we got some data from a client
//    int j = 0;
//    for(j = 0; j <= fdmax; j++) {
//        // send to everyone!
//        if (FD_ISSET(j, &master)) {
//            // except the listener and ourselves
//            if (j != listener && j != i) {
//                if (send(j, buf, nbytes, 0) == -1) {
//                    perror("send");
//                }
//            }
//        }
//    }
				}
			} // END handle data from client
		} // END got new incoming connection
	} // END looping through file descriptors
	return "";
}

void Socket::sendData(std::string data) {
	int j = 0;
	for (j = 0; j <= fdmax; j++) {
		// send to everyone
		if (FD_ISSET(j, &master)) {
			// no exception
			if (j != listener) {
				if (write(j, data.c_str(), data.length()) == -1) {
					//    perror("does not work\n");
				}
			}
		}
	}
}

const int Socket::getPort() const {
	return this->port;
}

void Socket::setPort(const int port) {
	this->port = port;
}

const std::string Socket::getAddress() const {
	return this->address;
}

void Socket::setAddress(const std::string address) {
	this->address = address;
}
