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

#include "Serializer.hh"

Serializer::Serializer() {
}

Serializer::~Serializer() {
}

const std::string Serializer::getConfig(
		std::map<std::string, std::string> configList, std::string key) const {
	bool n = configList.count(key);
	return (n) ? configList.at(key) : "";
}
