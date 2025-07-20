#include "FileUtils.h"
#include <algorithm>
#include <cctype>

std::string FileUtils::normalizePath(const std::string& path) {
    if (path.empty()) {
        return path;
    }
    
    std::string normalized = path;
    
    // 统一路径分隔符
    std::replace(normalized.begin(), normalized.end(), '\\', '/');
    
    // 移除重复的路径分隔符
    std::string::iterator new_end = std::unique(normalized.begin(), normalized.end(),
        [](char a, char b) { return a == '/' && b == '/'; });
    normalized.erase(new_end, normalized.end());
    
    // 移除末尾的路径分隔符（除非是根路径）
    if (normalized.length() > 1 && normalized.back() == '/') {
        normalized.pop_back();
    }
    
    return normalized;
}
