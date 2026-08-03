cmake_minimum_required(VERSION 3.22.1)

project("native_archive_engine")

# Define C++ Shared Library for Archive Engine
add_library(
    native_archive_engine
    SHARED
    native-archive.cpp
)

# Find Android NDK System Logging Library
find_library(
    log-lib
    log
)

# Link C++ Target Libraries
target_link_libraries(
    native_archive_engine
    ${log-lib}
)
