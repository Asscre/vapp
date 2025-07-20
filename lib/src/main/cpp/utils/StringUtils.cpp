#include "StringUtils.h"
#include <cstdarg>
#include <cstdio>
#include <memory>

std::string StringUtils::format(const char* format, ...) {
    if (!format) {
        return "";
    }
    
    va_list args;
    va_start(args, format);
    
    // 计算需要的缓冲区大小
    va_list args_copy;
    va_copy(args_copy, args);
    int size = vsnprintf(nullptr, 0, format, args_copy);
    va_end(args_copy);
    
    if (size <= 0) {
        va_end(args);
        return "";
    }
    
    // 分配缓冲区并格式化字符串
    std::unique_ptr<char[]> buffer(new char[size + 1]);
    vsnprintf(buffer.get(), size + 1, format, args);
    va_end(args);
    
    return std::string(buffer.get());
}
