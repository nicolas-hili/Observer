/*
 * Observer.hh
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
 */

#ifndef OBSERVER_HH_
#define OBSERVER_HH_

#include "Config.hh"
#include "Method.hh"
#include "Serializer.hh"

class Observer {
  private:
    Config config;
    Serializer* serializer;
    Method* method;

  public:
    Observer();
    ~Observer();
    void setMethod(Method * method);
    void setSerializer(Serializer * serializer);
    int connect();
    void configure();
	  bool canConnect();
    void disconnect();
    std::string read();
    void sendData(const Event event) const;
    void sendData(const std::string data) const;
	  const std::string serialize(const Event event) const;
	  Event parse(const std::string data) const;
};




#endif /* OBSERVER_HH_ */
