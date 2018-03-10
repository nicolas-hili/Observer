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

#include "CLIUtils.hh"

#include<string>
#include<vector>

namespace CLIUtils {

std::string trim(std::string str) {
	size_t strBegin = str.find_first_not_of(" \n\r\t");
	if (strBegin == std::string::npos)
		return ""; // no content

	size_t strEnd = str.find_last_not_of(" \n\r\t");
	size_t strRange = strEnd - strBegin + 1;
	return str.substr(strBegin, strRange);
}

std::vector<std::string> tokenizeCommand(const std::string cmd) {
	std::vector<std::string> cmdTokens;

	std::string command = trim(cmd);

	if (command.length() == 0)
		return cmdTokens;

	std::string tempS = "";
	bool newVar = false;
	bool quotation = false;

	for (unsigned int i = 0; i < command.length(); i++) {

		char c = command[i];
		bool isSpaceOrEOL = (c == ' ' || c == '\n');

		if (isSpaceOrEOL) {

			if (!newVar)
				continue;

			if (!quotation) {
				newVar = false;
				cmdTokens.push_back(trim(tempS));
				tempS = "";
			} else {
				tempS = tempS + c;
			}
		} else if (c == '"') {
			if (!quotation) {
				// open a quotation
				quotation = true;
				if (newVar) {
					cmdTokens.push_back(trim(tempS));
					tempS = "";
				}
				newVar = true;
			} else {
				quotation = false;
				newVar = false;
				cmdTokens.push_back(trim(tempS));
				tempS = "";
			}
		} else {
			newVar = true;
			tempS = tempS + c;
		}
	}
	if (newVar)
		cmdTokens.push_back(trim(tempS));

	return cmdTokens;
}

}

