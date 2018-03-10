/*
 * Serializer.hh
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
 */

#ifndef SERIALIZER_HH_
#define SERIALIZER_HH_

#include <map>
#include <string>
#include "Event.hh"

class Serializer {

public:
	Serializer();
	virtual ~Serializer();
	virtual void configure(std::map<std::string, std::string> configList) = 0;
	virtual const std::string serialize(const Event event) const = 0;
	virtual Event parse(const std::string data) const = 0;
	virtual const std::string getConfig(
			std::map<std::string, std::string> configList,
			std::string key) const;
};

#endif /* SERIALIZER_HH_ */
