/*******************************************************************************
 * Copyright (c) 2016-2017 School of Computing -- Queen's University
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Hili <hili@cs.queensu.ca>
 ******************************************************************************/

#include "MethodImpl.hh"

#include <stdio.h>
#include <iostream>
#include <string.h>
#include <fcntl.h>
#include <cerrno>
#include <unistd.h>

MethodImpl::MethodImpl() : Method() {
  this->setFdIn(STDIN_FILENO);
  this->setFdOut(STDOUT_FILENO);
  this->setBufferSize(1024);
}

MethodImpl::~MethodImpl() {
}

void MethodImpl::configure(std::map<std::string, std::string> configList) {
  (void) configList;
}

bool MethodImpl::canConnect() const {
	return true;
}

int MethodImpl::connect() {
  int flags = fcntl(this->fdIn, F_GETFL, 0);
  if(fcntl(this->fdIn, F_SETFL, flags | O_NONBLOCK)) {
  }
  return 1;
}

void MethodImpl::disconnect() {
}

std::string MethodImpl::readData() {
  int count;
  //char buffer[this->bufferSize];
  char *buffer = new char[this->bufferSize];
  count = read(this->fdIn, buffer, sizeof(buffer));

  if(count < 0 && errno == EAGAIN) {
      // If this condition passes, there is no data to be read
  }
  else if(count >= 0) {
    char *substr = new char[count+1];
    strncpy(substr, buffer, count);
    // null character manually added
    substr[count] = '\0';
    std::string out = substr;
    delete[] substr;
    return out;
  }
  else {
  }
  delete[] buffer;
	return "";
}

void MethodImpl::sendData(std::string data) {
  write(this->fdOut, data.c_str(), data.length());
}

int MethodImpl::getFdIn() const {
  return this->fdIn;
}

void MethodImpl::setFdIn(const int fd) {
  this->fdIn = fd;
}

int MethodImpl::getFdOut() const {
  return this->fdOut;
}

void MethodImpl::setFdOut(const int fd) {
  this->fdOut = fd;
}

int MethodImpl::getBufferSize() const {
  return this->bufferSize;
}

void MethodImpl::setBufferSize(const int size) {
  this->bufferSize = size;
}
