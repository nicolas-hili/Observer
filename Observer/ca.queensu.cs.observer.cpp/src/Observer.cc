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

#include "Observer.hh"
#include "Config.hh"
#include "Method.hh"
#include "Socket.hh"
#include "SharedMem.hh"
#include "Serializer.hh"
#include "Text.hh"
#include "Event.hh"
#include <stdio.h>
#include <dlfcn.h>
#include <iostream>

int main () {
    Config config;
    printf("number of entries: %i\n", config.load());

    Serializer* serializer;
    serializer = new Text();
    serializer->configure(config.getConfigList());

    Method* method;
    method = new SharedMem();
    method->configure(config.getConfigList());
    method->connect();
    printf("connection: %d\n", method->connect());

    // testing the serializer
    Event eventIn, eventOut;
    std::string strIn, strOut;

/*    eventIn.setSourceName("source name");
    eventIn.setParam("key1", "value1");
    eventIn.setParam("key2", "value2");
    eventIn.setParam("key3", "value3"); */
    eventIn.setEventSource(Event::EventSource::Command);
    eventIn.setEventKind(Event::EventKind::List);
    strIn = serializer->serialize(eventIn);

    eventOut = serializer->parse(strIn);
    strOut = serializer->serialize(eventOut);

    printf("%s\n", strIn.c_str());
    printf("%s\n", strOut.c_str());


    // Test list function
    method->sendData(strOut);

        while (true) {
		std::string event = method->read();
		if (event != ""){
			std::cout << event;
		}
    }

    method->disconnect();
    free(serializer);
    free(method);


    return 0;
}

