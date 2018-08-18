/*
 * Config.hh
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
 */

#ifndef CONFIG_HH_
#define CONFIG_HH_

#include <string>
#include <map>

class Config {

private:
	std::string configFileName;
	std::string fieldSeparator;
	std::string commentStr;
	std::string getConfigPath();
	std::map<std::string, std::string> configList;

public:
	Config();
	~Config();
	int load();
	void append(std::string key, std::string value);
	std::string get(std::string key);
	void setConfigFileName(const std::string configFileName);
	const std::string getConfigFileName() const;

	void setFieldSeparator(const std::string fieldSeparator);
	const std::string getFieldSeparator() const;

	void setCommentStr(const std::string commentStr);
	const std::string getCommentStr() const;

	const std::map<std::string, std::string> getConfigList() const;
};

#endif /* CONFIG_HH_ */
