/*
 * Method implementation for shared mem
 *
 *  Created on: Jun 28, 2016
 *      Author: mojtaba
 */

#ifndef METHOD_IMPL_HH_
#define METHOD_IMPL_HH_

#include "Method.hh"
#include <boost/interprocess/managed_shared_memory.hpp>
#include <boost/interprocess/allocators/allocator.hpp>
#include <boost/interprocess/containers/string.hpp>
#include <boost/interprocess/containers/deque.hpp>
#include <boost/interprocess/sync/scoped_lock.hpp>
#include <boost/interprocess/sync/named_mutex.hpp>

using namespace boost::interprocess;
typedef allocator<char, managed_shared_memory::segment_manager>   CharAllocator;
typedef basic_string<char, std::char_traits<char>, CharAllocator> ShmString;
typedef allocator<ShmString, managed_shared_memory::segment_manager> ShmStringAllocator;
typedef deque<ShmString, ShmStringAllocator> ShmStringDeque;
////  manage the mode that shared mem will be open

class MethodImpl: public Method {


public:
  enum Mode {
    Client,
    Server
  };

  enum Status {
    Initialized,
    Ready,
    Failed
  };

private:
	std::string name;
	std::string nameCmd;

	ShmStringDeque *sharedDeque;
	std::string queueName;
	managed_shared_memory observerSegment;

	ShmStringDeque *sharedDequeCmd;
	std::string queueNameCmd;
	//managed_shared_memory observerSegmentCmd;

	bool withLock;
	Status status;
	Mode mode;
	named_mutex * areaMutex;
	named_mutex * areaMutexCmd;
	size_t size;

public:
	MethodImpl();
	virtual ~MethodImpl();
	virtual void configure(std::map<std::string, std::string> configList);
	virtual bool canConnect() const;
	virtual int connect();
	virtual void disconnect();
	virtual std::string read();
	virtual void sendData(std::string data);

	const std::string& getName();
	const std::string& getNameCmd();
	void setName(const std::string& name);
	const std::string& getQueueName() const;
	const std::string& getQueueNameCmd() const;
	void setQueueName(const std::string& queueName);
	void pushBackString(std::string data);
	void pushBackStringCmd(std::string data);
	void safePushBackString(std::string data);
	void safePushBackStringCmd(std::string data);
	void safePushFrontString(std::string data);
	void pushFrontString(std::string data);
	std::string popFrontString();
	std::string popBackString();
	std::string popBackStringCmd();
	std::string getData(size_t index=0);
	int  getQueueSize();
	std::string safePopFrontString();
	std::string safePopBackString();
	std::string safePopBackStringCmd();
	std::string safeGetData(size_t index=0);
	int  safeGetQueueSize();
	Status getStatus() const;
	void setStatus(Status status);
	Mode getMode() const;
	void setMode(Mode mode);
	bool isWithLock() const;
	void setWithLock(bool withLock);
  size_t getSize() const;
  void setSize(const size_t size);
};

#endif /* METHOD_IMPL_HH_ */
