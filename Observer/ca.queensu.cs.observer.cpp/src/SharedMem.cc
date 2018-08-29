/*
 * SharedMem.cc
 *
 *  Created on: Jun 28, 2016
 *      Author: mojtaba
 */

#include "SharedMem.hh"
#include <boost/lexical_cast.hpp>
#include <boost/interprocess/managed_shared_memory.hpp>
#include <boost/interprocess/allocators/allocator.hpp>
#include <boost/interprocess/containers/string.hpp>
#include <boost/interprocess/containers/deque.hpp>
#include <boost/interprocess/sync/scoped_lock.hpp>
#include <boost/interprocess/sync/named_mutex.hpp>

using namespace boost::interprocess;

SharedMem::SharedMem() : Method() {

	sharedDeque=0;
	sharedDequeCmd=0;

	areaMutex=0;
	areaMutexCmd=0;

	this->setName("EventArea");
	this->setQueueName("EventQ");
	this->setSize(9999999);
	this->setWithLock(true);
	this->setStatus(Status::Initialized);
  this->setMode(Mode::Server);
}

SharedMem::~SharedMem() {
	if (this->getMode() == Mode::Server) {
    shared_memory_object::remove(this->name.c_str());
    shared_memory_object::remove(this->nameCmd.c_str());
  }
}

void SharedMem::configure(std::map<std::string, std::string> configList) {

  std::string name, qName, withLock, size, mode;

  name      = this->getConfig(configList, "sharedmem.name");
  qName     = this->getConfig(configList, "sharedmem.qName");
  withLock  = this->getConfig(configList, "sharedmem.withLock");
  size      = this->getConfig(configList, "sharedmem.size");
  mode      = this->getConfig(configList, "sharedmem.mode");

  if (!name.empty())
    this->setName(name);

  if (!qName.empty())
    this->setQueueName(qName);

  if (!withLock.empty())
    this->setWithLock(boost::lexical_cast<bool>(withLock));

  if (!size.empty())
    this->setSize(atoi(size.c_str()));

  if (!mode.empty())
    this->setMode(static_cast<Mode>(atoi(mode.c_str())));

  printf("name: %s, qName: %s, size: %d, withLock: %d\n",
      this->getName().c_str(),
      this->getQueueName().c_str(),
      (int)this->getSize(),
      this->isWithLock());
}

const bool SharedMem::canConnect() const {
  return true;
}

int SharedMem::connect() {

	if (this->getMode() == Mode::Server) {

		try {
      // Remove it if it is already created before
			shared_memory_object::remove(this->getName().c_str());
			//shared_memory_object::remove(this->getNameCmd().c_str());

			this->observerSegment = managed_shared_memory(create_only,
                                                    this->getName().c_str(),
                                                    this->getSize());

			//this->observerSegmentCmd = managed_shared_memory(create_only,
      //                                              this->getNameCmd().c_str(),
      //                                              this->getSize());


			const CharAllocator charallocator (observerSegment.get_segment_manager());
			const ShmStringAllocator stringInst(observerSegment.get_segment_manager());

			//const CharAllocator charallocatorCmd (observerSegmentCmd.get_segment_manager());
			//const ShmStringAllocator stringInstCmd(observerSegmentCmd.get_segment_manager());

			this->sharedDeque = observerSegment.construct<ShmStringDeque>(this->getQueueName().c_str())(stringInst);
			this->sharedDequeCmd = observerSegment.construct<ShmStringDeque>(this->getQueueNameCmd().c_str())(stringInst);
			//this->sharedDequeCmd = observerSegmentCmd.construct<ShmStringDeque>(this->getQueueNameCmd().c_str())(stringInstCmd);

			// Create the mutex also for locking
			named_mutex::remove((this->getName()).c_str());
			named_mutex::remove((this->getNameCmd()).c_str());

			this->areaMutex    = new named_mutex(create_only, (this->getName()).c_str());
			this->areaMutexCmd = new named_mutex(create_only, (this->getNameCmd()).c_str());
			this->setStatus(Status::Ready);
			return 0;

		}
		catch(boost::interprocess::interprocess_exception &ex) {
      printf("The Error happened in  shared memory setup:\n");
      this->setStatus(Status::Failed);
      return -1;
		}
  }
	if (this->getMode() == Mode::Client) {

		try {
			this->observerSegment=managed_shared_memory(open_only, this->getName().c_str());
			//this->observerSegmentCmd=managed_shared_memory(open_only, this->getNameCmd().c_str());
			// find and load related queue
			this->sharedDeque = observerSegment.find<ShmStringDeque>(this->getQueueName().c_str()).first;
			this->sharedDequeCmd = observerSegment.find<ShmStringDeque>(this->getQueueNameCmd().c_str()).first;
			//this->sharedDeque = segment.find<ShmStringVector>("MyVector").first;
			this->setStatus(Status::Ready);
			// Create the mutex
			this->areaMutex = new named_mutex(open_only, (this->getName()).c_str());
			this->areaMutexCmd = new named_mutex(open_only, (this->getNameCmd()).c_str());
			return 0;
		}
		catch (boost::interprocess::interprocess_exception &ex) {
			printf("The Error happened in  shared memory setup:\n");
			this->setStatus(Status::Failed);
			return -1;
		}

	}
	else {
		// no other mode exists now, will be extended in the future
		return -1;
  }

  return -1;
}

void SharedMem::disconnect() {
  // nothing here for now
}


std::string SharedMem::read() {
	if (this->getMode() == Mode::Server) {
    return safePopBackStringCmd();
  }
  else if (this->getMode() == Mode::Client) {
    return safePopBackString();
  }
  return "";
}

void SharedMem::sendData(std::string data) {
	if (this->getMode() == Mode::Server) {
    safePushBackString(data);
  }
  else if (this->getMode() == Mode::Client) {
    safePushBackStringCmd(data);
  }
}

const std::string& SharedMem::getName() {
		return this->name;
}

const std::string& SharedMem::getNameCmd() {
		return this->nameCmd;
}

void SharedMem::setName(const std::string& name) {
	this->name = name;
	this->nameCmd = name + "Cmd";
}

const size_t SharedMem::getSize() const {
  return this->size;
}

void SharedMem::setSize(const size_t size) {
  this->size = size;
}

const std::string& SharedMem::getQueueName() const {
	return queueName;
}

const std::string& SharedMem::getQueueNameCmd() const {
	return queueNameCmd;
}

void SharedMem::setQueueName(const std::string& queueName) {
	this->queueName = queueName;
	this->queueNameCmd = queueName + "Cmd";
}

Status SharedMem::getStatus() const {
	return status;
}

void SharedMem::setStatus(Status status) {
	this->status = status;
}

Mode SharedMem::getMode() const {
	return mode;
}

void SharedMem::setMode(Mode mode) {
	this->mode = mode;
}

bool SharedMem::isWithLock() const {
	return withLock;
}

void SharedMem::setWithLock(bool withLock) {
	this->withLock = withLock;
}

std::string SharedMem::popFrontString() {

	if (!this->sharedDeque->empty()) {
		const CharAllocator charallocator (observerSegment.get_segment_manager());
		ShmString tempString(charallocator);
		tempString= this->sharedDeque->front();
		this->sharedDeque->pop_front();
		return std::string(tempString.begin(),tempString.end());
	}
	else {
		return "";
  }
}

std::string SharedMem::popBackString() {

	if (!this->sharedDeque->empty()) {
		const CharAllocator charallocator (observerSegment.get_segment_manager());
		ShmString tempString(charallocator);
		tempString=this->sharedDeque->back();
		this->sharedDeque->pop_back();
		return std::string(tempString.begin(),tempString.end());
	}
	else {
		return "";
  }
}

std::string SharedMem::popBackStringCmd() {

	if (!this->sharedDequeCmd->empty()) {
		const CharAllocator charallocator (observerSegment.get_segment_manager());
		ShmString tempStringCmd(charallocator);
		tempStringCmd=this->sharedDequeCmd->back();
		this->sharedDequeCmd->pop_back();
		return std::string(tempStringCmd.begin(),tempStringCmd.end());
	}
	else {
		return "";
  }
}

std::string SharedMem::getData(size_t index) {

	if(this->sharedDeque->size() > index) {
    const CharAllocator charallocator (observerSegment.get_segment_manager());
    ShmString tempString(charallocator);
    tempString=this->sharedDeque->at(index);
    return std::string(tempString.begin(),tempString.end());
	}
	else {
    return "";
  }
}

int SharedMem::safeGetQueueSize() {
	{
    scoped_lock<named_mutex> lock(*areaMutex);
	  return this->sharedDeque->size();
  }
}

std::string SharedMem::safePopFrontString() {
	{
    scoped_lock<named_mutex> lock(*areaMutex);
	  return this->popFrontString();
  }
}

std::string SharedMem::safePopBackString()
{
	{
    scoped_lock<named_mutex> lock(*areaMutex);
	  return this->popBackString();
  }
}

std::string SharedMem::safePopBackStringCmd()
{
	{
    scoped_lock<named_mutex> lock(*areaMutexCmd);
	  return this->popBackStringCmd();
  }
}

std::string SharedMem::safeGetData(size_t index)
{
	{scoped_lock<named_mutex> lock(*areaMutex);
	return this->getData(index);}
}

int SharedMem::getQueueSize() {
  return this->sharedDeque->size();
}

void SharedMem::pushBackString(std::string data) {
	const CharAllocator charallocator (observerSegment.get_segment_manager());
	ShmString tempString(charallocator);
	tempString = data.c_str();
	this->sharedDeque->push_back(tempString);
}

void SharedMem::safePushFrontString(std::string data) {
	const CharAllocator charallocator (observerSegment.get_segment_manager());
	ShmString tempString(charallocator);
	tempString=data.c_str();
	{
    scoped_lock<named_mutex> lock(*areaMutex);
	  this->sharedDeque->push_back(tempString);
  }
}

void SharedMem::pushFrontString(std::string data) {
	const CharAllocator charallocator (observerSegment.get_segment_manager());
	ShmString tempString(charallocator);
	tempString=data.c_str();
	this->sharedDeque->push_front(tempString);
}

void SharedMem::safePushBackString(std::string data) {
	const CharAllocator charallocator (observerSegment.get_segment_manager());
	ShmString tempString(charallocator);
	tempString=data.c_str();
	{
    scoped_lock<named_mutex> lock(*areaMutex);
	  this->sharedDeque->push_front(tempString);
  }
}

void SharedMem::safePushBackStringCmd(std::string data) {
	const CharAllocator charallocator (observerSegment.get_segment_manager());
	//const CharAllocator charallocatorcmd (observerSegmentCmd.get_segment_manager());
	ShmString tempStringCmd(charallocator);
	tempStringCmd=data.c_str();
	{
    scoped_lock<named_mutex> lock(*areaMutexCmd);
	  this->sharedDequeCmd->push_front(tempStringCmd);
  }
}


