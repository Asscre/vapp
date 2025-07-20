#ifndef FILE_UTILS_H
#define FILE_UTILS_H

#include <string>

class FileUtils {
public:
    static std::string normalizePath(const std::string& path);
};

#endif // FILE_UTILS_H
