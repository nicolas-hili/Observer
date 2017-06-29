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

#include "Config.hh"

#include <stdio.h>
#include <fstream>
#include <iostream>
#include <unistd.h>

using namespace std;

Config::Config() {
	this->setConfigFileName("config");
	this->setFieldSeparator("=");
	this->setCommentStr("#");
}

Config::~Config() {
}

int Config::load() {

	int result = 0;
	std::string prefix = "";
	std::string configPath = this->getConfigPath();
	std::ifstream configFile(configPath.c_str());
	std::string line;

	if (configFile.is_open()) {
		while (std::getline(configFile, line)) {

			std::string key, value;

			// skip the space and comment line
			std::string::size_type nonSpaceCharIndex = line.find_first_not_of(
					" \f\t\v");
			if (nonSpaceCharIndex == std::string::npos)
				continue; // line is empty, skp it

			if (this->commentStr.find(line[nonSpaceCharIndex])
					!= std::string::npos)
				continue; // skip the line , it is a comment line

			// Check if it is a definition of a specific rule
			std::string::size_type prefixIndex = line.find("[",
					nonSpaceCharIndex);
			if (prefixIndex != std::string::npos) {

				std::string::size_type valueNonSpaceCharIndex =
						line.find_first_not_of(" \f\t\v", prefixIndex + 1);
				std::string p = line.substr(valueNonSpaceCharIndex);
				p.erase(p.find_last_not_of(" \f\t\v"));
				p.erase(p.find_last_not_of(" \f\t\v") + 1);
				prefix = p;
				continue;
			}

			// extract key value
			std::string::size_type sepIndex = line.find(
					this->getFieldSeparator(), nonSpaceCharIndex);
			if (sepIndex == std::string::npos)
				continue; // the line doesn't contain the seperator

			key = line.substr(nonSpaceCharIndex, sepIndex - nonSpaceCharIndex);
			key.erase(key.find_last_not_of(" \f\t\v") + 1);

			if (key.empty())
				continue; // the kwy should have value

			if (sepIndex + 1 == line.length())
				continue; // there is no value

/// extract the value
			std::string::size_type valueNonSpaceCharIndex =
					line.find_first_not_of(" \f\t\v", sepIndex + 1);

			if (valueNonSpaceCharIndex == std::string::npos)
				continue; // there is no value

			std::string::size_type valueLastNonSpaceCharIndex =
					line.find_last_not_of(" \f\t\v");
			value = line.substr(valueNonSpaceCharIndex,
					valueLastNonSpaceCharIndex - valueNonSpaceCharIndex + 1);
			if (!prefix.empty())
				key = prefix + "." + key;
			this->append(key, value);
			printf("%s=%s\n", key.c_str(), value.c_str());
			result = result + 1;
		}
	} else {
		std::cerr << "Error in reading configuration file from " << configPath
				<< std::endl;
		result = -1;
	}
	configFile.close();
	return result;
}

void Config::append(std::string key, std::string value) {
	if (!key.empty())
		this->configList[key] = value;
}

std::string Config::get(std::string key) {
	bool n = this->configList.count(key);
	return (n) ? this->configList.at(key) : "";
}

/**
 * Resolve the full path from the exec path
 */
std::string Config::getConfigPath() {

	char result[255];
	std::string path;

	ssize_t len = ::readlink("/proc/self/exe", result, sizeof(result));
	if (len != -1) {
		path = std::string(result);
	}

// find the last "/" position
	size_t n = path.rfind('/');

// remove the last fragment of the path
	path = (n > 0) ? path.substr(0, n) : "";

// return the full path
	return (path != "") ?
			path + "/" + this->configFileName : this->configFileName;
}

void Config::setConfigFileName(const std::string configFileName) {
	this->configFileName = configFileName;
}

const std::string Config::getConfigFileName() const {
	return this->configFileName;
}

void Config::setFieldSeparator(const std::string fieldSeparator) {
	this->fieldSeparator = fieldSeparator;
}

const std::string Config::getFieldSeparator() const {
	return this->fieldSeparator;
}

void Config::setCommentStr(const std::string commentStr) {
	this->commentStr = commentStr;
}

const std::string Config::getCommentStr() const {
	return this->commentStr;
}

const std::map<std::string, std::string> Config::getConfigList() const {
	return this->configList;
}
