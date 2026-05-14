# --- CABECERA REQUERIDA ---
cmake_minimum_required(VERSION 3.22.1)
project("gurry_native")

# ---------------------------------------------------------
# 1. DESCARGAR DEPENDENCIAS EXTERNAS PARA ANDROID
# ---------------------------------------------------------
include(FetchContent)

# Descargar Abseil (Librería base de Google)
FetchContent_Declare(
        absl
        GIT_REPOSITORY https://github.com/abseil/abseil-cpp.git
        GIT_TAG        20230125.3
)
set(ABSL_BUILD_TESTING OFF)
FetchContent_MakeAvailable(absl)

# Descargar ZSTD (Compresión)
FetchContent_Declare(
        zstd
        GIT_REPOSITORY https://github.com/facebook/zstd.git
        GIT_TAG        v1.5.5
        SOURCE_SUBDIR  build/cmake
)
set(ZSTD_BUILD_TESTS OFF)
set(ZSTD_BUILD_PROGRAMS OFF)
set(ZSTD_BUILD_SHARED OFF)
FetchContent_MakeAvailable(zstd)

# ---------------------------------------------------------
# 2. CONFIGURAR LONGFELLOW
# ---------------------------------------------------------
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/longfellow-zk/lib)
include_directories(${absl_SOURCE_DIR})
include_directories(${zstd_SOURCE_DIR}/lib)

file(GLOB_RECURSE LONGFELLOW_SRC
        "longfellow-zk/lib/*.cc"
        "longfellow-zk/lib/*.cpp"
        "longfellow-zk/lib/*.c"
)

list(FILTER LONGFELLOW_SRC EXCLUDE REGEX ".*_test\\.cc$")
list(FILTER LONGFELLOW_SRC EXCLUDE REGEX ".*googletest.*")
list(FILTER LONGFELLOW_SRC EXCLUDE REGEX ".*testing.*")
list(FILTER LONGFELLOW_SRC EXCLUDE REGEX ".*demo_.*")
list(FILTER LONGFELLOW_SRC EXCLUDE REGEX ".*_bench\\.cc$")
list(FILTER LONGFELLOW_SRC EXCLUDE REGEX ".*CMakeFiles.*")       # Ignorar basura de CMake
list(FILTER LONGFELLOW_SRC EXCLUDE REGEX ".*build.*")            # Ignorar builds previos de Linux
list(FILTER LONGFELLOW_SRC EXCLUDE REGEX ".*circuit_maker\\.cc$")# Ignorar ejecutables con main()

add_library(
        gurry_native
        SHARED
        prover.cpp
        ${LONGFELLOW_SRC}
)

# ---------------------------------------------------------
# 3. ENLAZAR TODO
# ---------------------------------------------------------
find_library(log-lib log)
find_package(openssl REQUIRED CONFIG)

target_link_libraries(
        gurry_native
        ${log-lib}
        openssl::crypto
        absl::cleanup
        libzstd_static
)