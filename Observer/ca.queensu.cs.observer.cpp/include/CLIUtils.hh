/*
 * CLIUtils.hh
 *
 *  Created on: Jun 29, 2017
 *      Author: nicolas
 */

#ifndef CLIUTILS_HH_
#define CLIUTILS_HH_

#include<string>
#include<vector>

namespace CLIUtils {

std::string trim(std::string str);
std::vector<std::string> tokenizeCommand(const std::string cmd);

}



#endif /* CLIUTILS_HH_ */
