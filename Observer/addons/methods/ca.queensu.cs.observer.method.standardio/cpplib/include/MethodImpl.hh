/*
 * Method implementation for standard io
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
 */

#ifndef METHOD_IMPL_HH_
#define METHOD_IMPL_HH_

#include "Method.hh"
#include <string>

class MethodImpl: public Method {

private:
  int fdIn, fdOut, bufferSize;

public:
	MethodImpl();
	virtual ~MethodImpl();
	virtual void configure(std::map<std::string, std::string> configList);
	virtual bool canConnect() const;
	virtual int connect();
	virtual void disconnect();
	virtual std::string readData();
	virtual void sendData(std::string data);

  int getFdIn() const;
  void setFdIn(const int fd); 

  int getFdOut() const;
  void setFdOut(const int fd); 

  int getBufferSize() const;
  void setBufferSize(const int size); 

};

#endif /* METHOD_IMPL_HH_ */
