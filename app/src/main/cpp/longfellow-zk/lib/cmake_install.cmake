# Install script for directory: /mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "/usr/local")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Install shared libraries without execute permission?
if(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  set(CMAKE_INSTALL_SO_NO_EXE "1")
endif()

# Is this installation the result of a crosscompile?
if(NOT DEFINED CMAKE_CROSSCOMPILING)
  set(CMAKE_CROSSCOMPILING "FALSE")
endif()

# Set default install directory permissions.
if(NOT DEFINED CMAKE_OBJDUMP)
  set(CMAKE_OBJDUMP "/usr/bin/objdump")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for each subdirectory.
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/testing/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/util/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/algebra/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/arrays/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/merkle/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/ligero/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/proto/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/random/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/sumcheck/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/gf2k/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/cbor/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/ec/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/zk/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/anoncred/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/base64/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/cbor_parser/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/compiler/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/ecdsa/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/jwt/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/logic/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/mac/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/mdoc/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/sha/cmake_install.cmake")
  include("/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/circuits/sha3/cmake_install.cmake")

endif()

if(CMAKE_INSTALL_COMPONENT)
  set(CMAKE_INSTALL_MANIFEST "install_manifest_${CMAKE_INSTALL_COMPONENT}.txt")
else()
  set(CMAKE_INSTALL_MANIFEST "install_manifest.txt")
endif()

string(REPLACE ";" "\n" CMAKE_INSTALL_MANIFEST_CONTENT
       "${CMAKE_INSTALL_MANIFEST_FILES}")
file(WRITE "/mnt/c/Users/rauld/Documents/4o+TFG/longfellow-zk/lib/${CMAKE_INSTALL_MANIFEST}"
     "${CMAKE_INSTALL_MANIFEST_CONTENT}")
