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

#include "Method.hh"
#include <stdio.h>

Method::Method() {
}

Method::~Method() {
}

bool Method::canConnect() const {
	return false;
}

const std::string Method::getConfig(
		std::map<std::string, std::string> configList, std::string key) const {
	bool n = configList.count(key);
	return (n) ? configList.at(key) : "";
}

void Method::configure(std::map<std::string, std::string> configList) {
  (void)configList;
}
