/*
 * Method.hh
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
 */

#ifndef METHOD_HH_
#define METHOD_HH_

#include <map>
#include <string>

class Method {

public:
	Method();
	virtual ~Method();
	virtual void configure(std::map<std::string, std::string> configList);
	virtual bool canConnect() const;
	virtual int connect() = 0;
	virtual void disconnect() = 0;
	virtual std::string read() = 0;
	virtual void sendData(std::string data) = 0;
	virtual const std::string getConfig(
			std::map<std::string, std::string> configList,
			std::string key) const;
};

#endif /* METHOD_HH_ */

