# This should match the schema defined in external_deps.bzl.
REPOSITORY_LOCATIONS_SPEC = dict(
    bazel_compdb = dict(
        project_name = "bazel-compilation-database",
        project_desc = "Clang JSON compilation database support for Bazel",
        project_url = "https://github.com/grailbio/bazel-compilation-database",
        version = "0.5.2",
        sha256 = "d32835b26dd35aad8fd0ba0d712265df6565a3ad860d39e4c01ad41059ea7eda",
        strip_prefix = "bazel-compilation-database-{version}",
        urls = ["https://github.com/grailbio/bazel-compilation-database/archive/{version}.tar.gz"],
        release_date = "2021-09-10",
        use_category = ["build"],
    ),
    bazel_gazelle = dict(
        project_name = "Gazelle",
        project_desc = "Bazel BUILD file generator for Go projects",
        project_url = "https://github.com/bazelbuild/bazel-gazelle",
        version = "0.24.0",
        sha256 = "de69a09dc70417580aabf20a28619bb3ef60d038470c7cf8442fafcf627c21cb",
        urls = ["https://github.com/bazelbuild/bazel-gazelle/releases/download/v{version}/bazel-gazelle-v{version}.tar.gz"],
        release_date = "2021-10-11",
        use_category = ["build"],
    ),
    bazel_toolchains = dict(
        project_name = "bazel-toolchains",
        project_desc = "Bazel toolchain configs for RBE",
        project_url = "https://github.com/bazelbuild/bazel-toolchains",
        version = "5.1.1",
        sha256 = "e52789d4e89c3e2dc0e3446a9684626a626b6bec3fde787d70bae37c6ebcc47f",
        strip_prefix = "bazel-toolchains-{version}",
        urls = [
            "https://github.com/bazelbuild/bazel-toolchains/archive/v{version}.tar.gz",
        ],
        release_date = "2021-11-30",
        use_category = ["build"],
    ),
    build_bazel_rules_apple = dict(
        project_name = "Apple Rules for Bazel",
        project_desc = "Bazel rules for Apple platforms",
        project_url = "https://github.com/bazelbuild/rules_apple",
        version = "0.34.0",
        sha256 = "4161b2283f80f33b93579627c3bd846169b2d58848b0ffb29b5d4db35263156a",
        urls = ["https://github.com/bazelbuild/rules_apple/releases/download/{version}/rules_apple.{version}.tar.gz"],
        release_date = "2022-03-23",
        use_category = ["build"],
    ),
    envoy_build_tools = dict(
        project_name = "envoy-build-tools",
        project_desc = "Common build tools shared by the Envoy/UDPA ecosystem",
        project_url = "https://github.com/envoyproxy/envoy-build-tools",
        version = "f710be3099b65a2f260a632f8336a2c18e8324b9",
        sha256 = "c7064405ab9dc4e04343147fc5fd399e90d25ccb849ede0d3e288b2e54c04634",
        strip_prefix = "envoy-build-tools-{version}",
        urls = ["https://github.com/envoyproxy/envoy-build-tools/archive/{version}.tar.gz"],
        release_date = "2022-03-16",
        use_category = ["build"],
    ),
    boringssl = dict(
        project_name = "BoringSSL",
        project_desc = "Minimal OpenSSL fork",
        project_url = "https://github.com/google/boringssl",
        # To update BoringSSL, which tracks Chromium releases:
        # 1. Open https://omahaproxy.appspot.com/ and note <current_version> of linux/beta release.
        # 2. Open https://chromium.googlesource.com/chromium/src/+/refs/tags/<current_version>/DEPS and note <boringssl_revision>.
        # 3. Find a commit in BoringSSL's "master-with-bazel" branch that merges <boringssl_revision>.
        #
        # chromium-100.0.4896.30 (linux/beta)
        version = "cacb5526268191ab52e3a8b2d71f686115776646",
        sha256 = "fd72798ee22beb9f052d792d6c701d3ea94183c2e5b94e737866a53152b46f41",
        strip_prefix = "boringssl-{version}",
        urls = ["https://github.com/google/boringssl/archive/{version}.tar.gz"],
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2022-02-08",
        cpe = "cpe:2.3:a:google:boringssl:*",
    ),
    boringssl_fips = dict(
        project_name = "BoringSSL (FIPS)",
        project_desc = "FIPS compliant BoringSSL",
        project_url = "https://boringssl.googlesource.com/boringssl/+/master/crypto/fipsmodule/FIPS.md",
        version = "fips-20190808",
        sha256 = "3b5fdf23274d4179c2077b5e8fa625d9debd7a390aac1d165b7e47234f648bb8",
        urls = ["https://commondatastorage.googleapis.com/chromium-boringssl-fips/boringssl-ae223d6138807a13006342edfeef32e813246b39.tar.xz"],
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2019-08-08",
        cpe = "cpe:2.3:a:google:boringssl:*",
    ),
    com_google_absl = dict(
        project_name = "Abseil",
        project_desc = "Open source collection of C++ libraries drawn from the most fundamental pieces of Google’s internal codebase",
        project_url = "https://abseil.io/",
        version = "6f43f5bb398b6685575b36874e36cf1695734df1",
        sha256 = "5ca73792af71ab962ee81cdf575f79480704b8fb87e16ca8f1dc1e9b6822611e",
        strip_prefix = "abseil-cpp-{version}",
        urls = ["https://github.com/abseil/abseil-cpp/archive/{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2022-04-04",
        cpe = "N/A",
    ),
    com_github_axboe_liburing = dict(
        project_name = "liburing",
        project_desc = "C helpers to set up and tear down io_uring instances",
        project_url = "https://github.com/axboe/liburing",
        version = "2.1",
        sha256 = "f1e0500cb3934b0b61c5020c3999a973c9c93b618faff1eba75aadc95bb03e07",
        strip_prefix = "liburing-liburing-{version}",
        urls = ["https://github.com/axboe/liburing/archive/liburing-{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2021-09-09",
        cpe = "N/A",
    ),
    com_github_c_ares_c_ares = dict(
        project_name = "c-ares",
        project_desc = "C library for asynchronous DNS requests",
        project_url = "https://c-ares.haxx.se/",
        version = "1.18.1",
        sha256 = "1a7d52a8a84a9fbffb1be9133c0f6e17217d91ea5a6fa61f6b4729cda78ebbcf",
        strip_prefix = "c-ares-{version}",
        urls = ["https://github.com/c-ares/c-ares/releases/download/cares-{underscore_version}/c-ares-{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2021-10-27",
        cpe = "cpe:2.3:a:c-ares_project:c-ares:*",
    ),
    com_github_circonus_labs_libcircllhist = dict(
        project_name = "libcircllhist",
        project_desc = "An implementation of Circonus log-linear histograms",
        project_url = "https://github.com/circonus-labs/libcircllhist",
        version = "63a16dd6f2fc7bc841bb17ff92be8318df60e2e1",
        sha256 = "8165aa25e529d7d4b9ae849d3bf30371255a99d6db0421516abcff23214cdc2c",
        strip_prefix = "libcircllhist-{version}",
        urls = ["https://github.com/circonus-labs/libcircllhist/archive/{version}.tar.gz"],
        use_category = ["controlplane", "observability_core", "dataplane_core"],
        release_date = "2019-02-11",
        cpe = "N/A",
    ),
    com_github_cyan4973_xxhash = dict(
        project_name = "xxHash",
        project_desc = "Extremely fast hash algorithm",
        project_url = "https://github.com/Cyan4973/xxHash",
        version = "0.8.1",
        sha256 = "3bb6b7d6f30c591dd65aaaff1c8b7a5b94d81687998ca9400082c739a690436c",
        strip_prefix = "xxHash-{version}",
        urls = ["https://github.com/Cyan4973/xxHash/archive/v{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2021-11-29",
        cpe = "N/A",
    ),
    com_github_envoyproxy_sqlparser = dict(
        project_name = "C++ SQL Parser Library",
        project_desc = "Forked from Hyrise SQL Parser",
        project_url = "https://github.com/envoyproxy/sql-parser",
        version = "3b40ba2d106587bdf053a292f7e3bb17e818a57f",
        sha256 = "96c10c8e950a141a32034f19b19cdeb1da48fe859cf96ae5e19f894f36c62c71",
        strip_prefix = "sql-parser-{version}",
        urls = ["https://github.com/envoyproxy/sql-parser/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = [
            "envoy.filters.network.mysql_proxy",
            "envoy.filters.network.postgres_proxy",
        ],
        release_date = "2020-06-10",
        cpe = "N/A",
    ),
    com_github_mirror_tclap = dict(
        project_name = "tclap",
        project_desc = "Small, flexible library that provides a simple interface for defining and accessing command line arguments",
        project_url = "http://tclap.sourceforge.net",
        version = "1.2.5",
        sha256 = "7e87d13734076fa4f626f6144ce9a02717198b3f054341a6886e2107b048b235",
        strip_prefix = "tclap-{version}",
        urls = ["https://github.com/mirror/tclap/archive/v{version}.tar.gz"],
        release_date = "2021-11-01",
        use_category = ["other"],
        cpe = "cpe:2.3:a:tclap_project:tclap:*",
    ),
    com_github_fmtlib_fmt = dict(
        project_name = "fmt",
        project_desc = "{fmt} is an open-source formatting library providing a fast and safe alternative to C stdio and C++ iostreams",
        project_url = "https://fmt.dev",
        version = "8.1.1",
        sha256 = "23778bad8edba12d76e4075da06db591f3b0e3c6c04928ced4a7282ca3400e5d",
        strip_prefix = "fmt-{version}",
        urls = ["https://github.com/fmtlib/fmt/releases/download/{version}/fmt-{version}.zip"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2022-01-06",
        cpe = "cpe:2.3:a:fmt:fmt:*",
    ),
    com_github_gabime_spdlog = dict(
        project_name = "spdlog",
        project_desc = "Very fast, header-only/compiled, C++ logging library",
        project_url = "https://github.com/gabime/spdlog",
        version = "1.9.2",
        sha256 = "6fff9215f5cb81760be4cc16d033526d1080427d236e86d70bb02994f85e3d38",
        strip_prefix = "spdlog-{version}",
        urls = ["https://github.com/gabime/spdlog/archive/v{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2021-08-12",
        cpe = "N/A",
    ),
    com_github_google_libsxg = dict(
        project_name = "libsxg",
        project_desc = "Signed HTTP Exchange library",
        project_url = "https://github.com/google/libsxg",
        version = "beaa3939b76f8644f6833267e9f2462760838f18",
        sha256 = "082bf844047a9aeec0d388283d5edc68bd22bcf4d32eb5a566654ae89956ad1f",
        strip_prefix = "libsxg-{version}",
        urls = ["https://github.com/google/libsxg/archive/{version}.tar.gz"],
        use_category = ["other"],
        extensions = ["envoy.filters.http.sxg"],
        release_date = "2021-07-08",
        cpe = "N/A",
    ),
    com_github_google_tcmalloc = dict(
        project_name = "tcmalloc",
        project_desc = "Fast, multi-threaded malloc implementation",
        project_url = "https://github.com/google/tcmalloc",
        version = "a08f5483182f41ce626e3f66662966342bb448fa",
        sha256 = "a3bd1b414ca534b5975187132f9a7ca6e392bc2c6379bb14bf096f403035a6c6",
        strip_prefix = "tcmalloc-{version}",
        urls = ["https://github.com/google/tcmalloc/archive/{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2022-04-07",
        cpe = "N/A",
    ),
    com_github_gperftools_gperftools = dict(
        project_name = "gperftools",
        project_desc = "tcmalloc and profiling libraries",
        project_url = "https://github.com/gperftools/gperftools",
        version = "2.9.1",
        sha256 = "ea566e528605befb830671e359118c2da718f721c27225cbbc93858c7520fee3",
        strip_prefix = "gperftools-{version}",
        urls = ["https://github.com/gperftools/gperftools/releases/download/gperftools-{version}/gperftools-{version}.tar.gz"],
        release_date = "2021-03-03",
        use_category = ["dataplane_core", "controlplane"],
        cpe = "cpe:2.3:a:gperftools_project:gperftools:*",
    ),
    com_github_grpc_grpc = dict(
        project_name = "gRPC",
        project_desc = "gRPC C core library",
        project_url = "https://grpc.io",
        version = "a3ae8e00a2c5553c806e83fae83e33f0198913f0",
        sha256 = "1ccc2056b68b81ada8df61310e03dfa0541c34821fd711654d0590a7321db9c8",
        strip_prefix = "grpc-{version}",
        urls = ["https://github.com/grpc/grpc/archive/{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2021-06-07",
        cpe = "cpe:2.3:a:grpc:grpc:*",
    ),
    com_github_unicode_org_icu = dict(
        project_name = "ICU Library",
        project_desc = "Development files for International Components for Unicode",
        project_url = "https://github.com/unicode-org/icu",
        version = "70-1",
        sha256 = "f30d670bdc03ba999638a2d2511952ab94adf204d0e14898666f2e0cacb7fef1",
        strip_prefix = "icu-release-{version}",
        urls = ["https://github.com/unicode-org/icu/archive/release-{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = ["envoy.filters.http.language"],
        release_date = "2021-10-28",
        cpe = "N/A",
    ),
    com_github_intel_ipp_crypto_crypto_mb = dict(
        project_name = "libipp-crypto",
        project_desc = "Intel® Integrated Performance Primitives Cryptography",
        project_url = "https://github.com/intel/ipp-crypto",
        version = "2021.5",
        sha256 = "0b277548c59e6bfe489e634d622b54be3708086fc006a441d39922c2d6d43f0d",
        strip_prefix = "ipp-crypto-ippcp_{version}",
        urls = ["https://github.com/intel/ipp-crypto/archive/ippcp_{version}.tar.gz"],
        release_date = "2021-12-21",
        use_category = ["dataplane_ext"],
        extensions = ["envoy.tls.key_providers.cryptomb"],
        cpe = "cpe:2.3:a:intel:cryptography_for_intel_integrated_performance_primitives:*",
    ),
    com_github_luajit_luajit = dict(
        project_name = "LuaJIT",
        project_desc = "Just-In-Time compiler for Lua",
        project_url = "https://luajit.org",
        # The last release version, 2.1.0-beta3 has a number of CVEs filed
        # against it. These may not impact correct non-malicious Lua code, but for prudence we bump.
        version = "1d8b747c161db457e032a023ebbff511f5de5ec2",
        sha256 = "20a159c38a98ecdb6368e8d655343b6036622a29a1621da9dc303f7ed9bf37f3",
        strip_prefix = "LuaJIT-{version}",
        urls = ["https://github.com/LuaJIT/LuaJIT/archive/{version}.tar.gz"],
        release_date = "2020-10-12",
        use_category = ["dataplane_ext"],
        extensions = ["envoy.filters.http.lua"],
        cpe = "cpe:2.3:a:luajit:luajit:*",
    ),
    com_github_moonjit_moonjit = dict(
        project_name = "Moonjit",
        project_desc = "LuaJIT fork with wider platform support",
        project_url = "https://github.com/moonjit/moonjit",
        version = "2.2.0",
        sha256 = "83deb2c880488dfe7dd8ebf09e3b1e7613ef4b8420de53de6f712f01aabca2b6",
        strip_prefix = "moonjit-{version}",
        urls = ["https://github.com/moonjit/moonjit/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = ["envoy.filters.http.lua"],
        release_date = "2020-01-14",
        cpe = "cpe:2.3:a:moonjit_project:moonjit:*",
    ),
    com_github_nghttp2_nghttp2 = dict(
        project_name = "Nghttp2",
        project_desc = "Implementation of HTTP/2 and its header compression algorithm HPACK in Cimplementation of HTTP/2 and its header compression algorithm HPACK in C",
        project_url = "https://nghttp2.org",
        version = "1.46.0",
        sha256 = "4b6d11c85f2638531d1327fe1ed28c1e386144e8841176c04153ed32a4878208",
        strip_prefix = "nghttp2-{version}",
        urls = ["https://github.com/nghttp2/nghttp2/releases/download/v{version}/nghttp2-{version}.tar.gz"],
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2021-10-19",
        cpe = "cpe:2.3:a:nghttp2:nghttp2:*",
    ),
# removed from sc_envoy build (eedrak)
#    io_hyperscan = dict(
#        project_name = "Hyperscan",
#        project_desc = "High-performance regular expression matching library",
#        project_url = "https://hyperscan.io",
#        version = "5.4.0",
#        sha256 = "e51aba39af47e3901062852e5004d127fa7763b5dbbc16bcca4265243ffa106f",
#        strip_prefix = "hyperscan-{version}",
#        urls = ["https://github.com/intel/hyperscan/archive/v{version}.tar.gz"],
#        use_category = ["controlplane", "dataplane_core"],
#        release_date = "2021-01-13",
#        cpe = "N/A",
#   ),
    skywalking_data_collect_protocol = dict(
        project_name = "skywalking-data-collect-protocol",
        project_desc = "Data Collect Protocols of Apache SkyWalking",
        project_url = "https://github.com/apache/skywalking-data-collect-protocol",
        version = "8.9.1",
        name = "skywalking_data_collect_protocol",
        sha256 = "49bd689b9c1c0ea12064bd35581689cef7835e5ac15d335dc425fbfc2029aa90",
        urls = ["https://github.com/apache/skywalking-data-collect-protocol/archive/v{version}.tar.gz"],
        strip_prefix = "skywalking-data-collect-protocol-{version}",
        use_category = ["observability_ext"],
        extensions = ["envoy.tracers.skywalking"],
        release_date = "2021-12-11",
        cpe = "cpe:2.3:a:apache:skywalking:*",
    ),
    com_github_skyapm_cpp2sky = dict(
        project_name = "cpp2sky",
        project_desc = "C++ SDK for Apache SkyWalking",
        project_url = "https://github.com/SkyAPM/cpp2sky",
        sha256 = "f65b1054bd6eadadff0618f272f6d645a1ec933fa14af922a8e3c39603e45eaf",
        version = "0.3.1",
        strip_prefix = "cpp2sky-{version}",
        urls = ["https://github.com/SkyAPM/cpp2sky/archive/v{version}.tar.gz"],
        use_category = ["observability_ext"],
        extensions = ["envoy.tracers.skywalking"],
        release_date = "2021-06-17",
        cpe = "N/A",
    ),
    com_github_libevent_libevent = dict(
        project_name = "libevent",
        project_desc = "Event notification library",
        project_url = "https://libevent.org",
        # This SHA includes the new "prepare" and "check" watchers, used for event loop performance
        # stats (see https://github.com/libevent/libevent/pull/793) and the fix for a race condition
        # in the watchers (see https://github.com/libevent/libevent/pull/802).
        # This also includes the fixes for https://github.com/libevent/libevent/issues/806
        # and https://github.com/envoyproxy/envoy-mobile/issues/215.
        # This also includes the fixes for Phantom events with EV_ET (see
        # https://github.com/libevent/libevent/issues/984).
        # This also includes the wepoll backend for Windows (see
        # https://github.com/libevent/libevent/pull/1006)
        # TODO(adip): Update to v2.2 when it is released.
        version = "62c152d9a7cd264b993dad730c4163c6ede2e0a3",
        sha256 = "4c80e5fe044ce5f8055b20a2f141ee32ec2614000f3e95d2aa81611a4c8f5213",
        strip_prefix = "libevent-{version}",
        urls = ["https://github.com/libevent/libevent/archive/{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2020-07-28",
        cpe = "cpe:2.3:a:libevent_project:libevent:*",
    ),
# removed from sc_envoy build (eedrak)
#    net_colm_open_source_ragel = dict(
#        project_name = "Ragel",
#        project_desc = "Ragel State Machine Compiler",
#        project_url = "https://www.colm.net/open-source/ragel/",
#        version = "6.10",
#        sha256 = "5f156edb65d20b856d638dd9ee2dfb43285914d9aa2b6ec779dac0270cd56c3f",
#        strip_prefix = "ragel-{version}",
#        urls = ["https://www.colm.net/files/ragel/ragel-{version}.tar.gz"],
#        use_category = ["controlplane", "dataplane_core"],
#        release_date = "2017-03-24",
#        cpe = "N/A",
#    ),
    # This should be removed, see https://github.com/envoyproxy/envoy/issues/13261.
    net_zlib = dict(
        project_name = "zlib",
        project_desc = "zlib compression library",
        project_url = "https://zlib.net",
        version = "79baebe50e4d6b73ae1f8b603f0ef41300110aa3",
        # Use the dev branch of zlib to resolve fuzz bugs and out of bound
        # errors resulting in crashes in zlib 1.2.11.
        # TODO(asraa): Remove when zlib > 1.2.11 is released.
        sha256 = "155a8f8c1a753fb05b16a1b0cc0a0a9f61a78e245f9e0da483d13043b3bcbf2e",
        strip_prefix = "zlib-{version}",
        urls = ["https://github.com/madler/zlib/archive/{version}.tar.gz"],
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2019-04-14",
        cpe = "cpe:2.3:a:gnu:zlib:*",
    ),
    org_boost = dict(
        project_name = "Boost",
        project_desc = "Boost C++ source libraries",
        project_url = "https://www.boost.org/",
        version = "1.78.0",
        sha256 = "94ced8b72956591c4775ae2207a9763d3600b30d9d7446562c552f0a14a63be7",
        strip_prefix = "boost_{underscore_version}",
        urls = ["https://boostorg.jfrog.io/artifactory/main/release/{version}/source/boost_{underscore_version}.tar.gz"],
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2021-12-08",
        cpe = "cpe:2.3:a:boost:boost:*",
    ),
    org_brotli = dict(
        project_name = "brotli",
        project_desc = "brotli compression library",
        project_url = "https://brotli.org",
        # Use the dev branch of brotli to resolve compilation issues.
        # TODO(rojkov): Remove when brotli > 1.0.9 is released.
        version = "0cd2e3926e95e7e2930f57ae3f4885508d462a25",
        sha256 = "93810780e60304b51f2c9645fe313a6e4640711063ed0b860cfa60999dd256c5",
        strip_prefix = "brotli-{version}",
        urls = ["https://github.com/google/brotli/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = [
            "envoy.compression.brotli.compressor",
            "envoy.compression.brotli.decompressor",
        ],
        release_date = "2020-09-08",
        cpe = "cpe:2.3:a:google:brotli:*",
    ),
    com_github_facebook_zstd = dict(
        project_name = "zstd",
        project_desc = "zstd compression library",
        project_url = "https://facebook.github.io/zstd",
        version = "1.5.2",
        sha256 = "f7de13462f7a82c29ab865820149e778cbfe01087b3a55b5332707abf9db4a6e",
        strip_prefix = "zstd-{version}",
        urls = ["https://github.com/facebook/zstd/archive/v{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = [
            "envoy.compression.zstd.compressor",
            "envoy.compression.zstd.decompressor",
        ],
        release_date = "2022-01-20",
        cpe = "cpe:2.3:a:facebook:zstandard:*",
    ),
    com_github_zlib_ng_zlib_ng = dict(
        project_name = "zlib-ng",
        project_desc = "zlib fork (higher performance)",
        project_url = "https://github.com/zlib-ng/zlib-ng",
        version = "2.0.6",
        sha256 = "8258b75a72303b661a238047cb348203d88d9dddf85d480ed885f375916fcab6",
        strip_prefix = "zlib-ng-{version}",
        urls = ["https://github.com/zlib-ng/zlib-ng/archive/{version}.tar.gz"],
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2021-12-24",
        cpe = "N/A",
    ),
    com_github_jbeder_yaml_cpp = dict(
        project_name = "yaml-cpp",
        project_desc = "YAML parser and emitter in C++ matching the YAML 1.2 spec",
        project_url = "https://github.com/jbeder/yaml-cpp",
        version = "db6deedcd301754723065e0bbb1b75927c5b49c7",
        sha256 = "387d7f25467312ca59068081f9a25bbab02bb6af32fd3e0aec1bd59163558171",
        strip_prefix = "yaml-cpp-{version}",
        urls = ["https://github.com/jbeder/yaml-cpp/archive/{version}.tar.gz"],
        # YAML is also used for runtime as well as controlplane. It shouldn't appear on the
        # dataplane but we can't verify this automatically due to code structure today.
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2021-07-23",
        cpe = "cpe:2.3:a:yaml-cpp_project:yaml-cpp:*",
    ),
    com_github_google_jwt_verify = dict(
        project_name = "jwt_verify_lib",
        project_desc = "JWT verification library for C++",
        project_url = "https://github.com/google/jwt_verify_lib",
        version = "e5d6cf7067495b0868787e1fd1e75cef3242a840",
        sha256 = "0d294dc8697049a0d7f2aaa81d08713fea581061c5359d6edb229b3e7c6cf58e",
        strip_prefix = "jwt_verify_lib-{version}",
        urls = ["https://github.com/google/jwt_verify_lib/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = ["envoy.filters.http.jwt_authn"],
        release_date = "2021-03-05",
        cpe = "N/A",
    ),
    com_github_alibaba_hessian2_codec = dict(
        project_name = "hessian2-codec",
        project_desc = "hessian2-codec is a C++ library for hessian2 codec",
        project_url = "https://github.com/alibaba/hessian2-codec.git",
        version = "dd8e05487a27b367b90ce81f4e6e6f62d693a212",
        sha256 = "93260c54406e11b7be078a7ea120f7ab0df475c733e68d010fde400c5c8c8162",
        strip_prefix = "hessian2-codec-{version}",
        urls = ["https://github.com/alibaba/hessian2-codec/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = ["envoy.filters.network.dubbo_proxy"],
        release_date = "2021-04-05",
        cpe = "N/A",
    ),
    com_github_nlohmann_json = dict(
        project_name = "nlohmann JSON",
        project_desc = "Fast JSON parser/generator for C++",
        project_url = "https://nlohmann.github.io/json",
        version = "3.10.5",
        sha256 = "5daca6ca216495edf89d167f808d1d03c4a4d929cef7da5e10f135ae1540c7e4",
        strip_prefix = "json-{version}",
        urls = ["https://github.com/nlohmann/json/archive/v{version}.tar.gz"],
        # This will be a replacement for rapidJSON used in extensions and may also be a fast
        # replacement for protobuf JSON.
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2022-01-03",
        cpe = "cpe:2.3:a:json-for-modern-cpp_project:json-for-modern-cpp:*",
    ),
    # This is an external dependency needed while running the
    # envoy docker image. A bazel target has been created since
    # there is no binary package available for the utility on Ubuntu
    # which is the base image used to build an envoy container.
    # This is not needed to build an envoy binary or run tests.
    com_github_ncopa_suexec = dict(
        project_name = "su-exec",
        project_desc = "Utility to switch user and group id, setgroups and exec",
        project_url = "https://github.com/ncopa/su-exec",
        version = "212b75144bbc06722fbd7661f651390dc47a43d1",
        sha256 = "939782774079ec156788ea3e04dd5e340e993544f4296be76a9c595334ca1779",
        strip_prefix = "su-exec-{version}",
        urls = ["https://github.com/ncopa/su-exec/archive/{version}.tar.gz"],
        use_category = ["other"],
        release_date = "2019-09-18",
        cpe = "N/A",
    ),
    com_google_protobuf = dict(
        project_name = "Protocol Buffers",
        project_desc = "Language-neutral, platform-neutral extensible mechanism for serializing structured data",
        project_url = "https://developers.google.com/protocol-buffers",
        version = "3.19.4",
        # When upgrading the protobuf library, please re-run
        # test/common/json:gen_excluded_unicodes to recompute the ranges
        # excluded from differential fuzzing that are populated in
        # test/common/json/json_sanitizer_test_util.cc.
        sha256 = "ba0650be1b169d24908eeddbe6107f011d8df0da5b1a5a4449a913b10e578faf",
        strip_prefix = "protobuf-{version}",
        urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{version}/protobuf-all-{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane"],
        release_date = "2022-01-28",
        cpe = "cpe:2.3:a:google:protobuf:*",
    ),
    io_bazel_rules_go = dict(
        project_name = "Go rules for Bazel",
        project_desc = "Bazel rules for the Go language",
        project_url = "https://github.com/bazelbuild/rules_go",
        version = "0.31.0",
        sha256 = "f2dcd210c7095febe54b804bb1cd3a58fe8435a909db2ec04e31542631cf715c",
        urls = ["https://github.com/bazelbuild/rules_go/releases/download/v{version}/rules_go-v{version}.zip"],
        use_category = ["build", "api"],
        release_date = "2022-03-21",
        implied_untracked_deps = [
            "com_github_golang_protobuf",
            "io_bazel_rules_nogo",
            "org_golang_google_protobuf",
            "org_golang_x_tools",
        ],
    ),
    rules_cc = dict(
        project_name = "C++ rules for Bazel",
        project_desc = "Bazel rules for the C++ language",
        project_url = "https://github.com/bazelbuild/rules_cc",
        version = "0.0.1",
        sha256 = "4dccbfd22c0def164c8f47458bd50e0c7148f3d92002cdb459c2a96a68498241",
        urls = ["https://github.com/bazelbuild/rules_cc/releases/download/{version}/rules_cc-{version}.tar.gz"],
        release_date = "2021-10-07",
        use_category = ["build"],
    ),
    rules_foreign_cc = dict(
        project_name = "Rules for using foreign build systems in Bazel",
        project_desc = "Rules for using foreign build systems in Bazel",
        project_url = "https://github.com/bazelbuild/rules_foreign_cc",
        version = "0.7.1",
        sha256 = "bcd0c5f46a49b85b384906daae41d277b3dc0ff27c7c752cc51e43048a58ec83",
        strip_prefix = "rules_foreign_cc-{version}",
        urls = ["https://github.com/bazelbuild/rules_foreign_cc/archive/{version}.tar.gz"],
        release_date = "2022-01-03",
        use_category = ["build", "dataplane_core", "controlplane"],
    ),
    rules_python = dict(
        project_name = "Python rules for Bazel",
        project_desc = "Bazel rules for the Python language",
        project_url = "https://github.com/bazelbuild/rules_python",
        version = "0.7.0",
        sha256 = "15f84594af9da06750ceb878abbf129241421e3abbd6e36893041188db67f2fb",
        release_date = "2022-03-11",
        strip_prefix = "rules_python-{version}",
        urls = ["https://github.com/bazelbuild/rules_python/archive/{version}.tar.gz"],
        use_category = ["build"],
    ),
    rules_pkg = dict(
        project_name = "Packaging rules for Bazel",
        project_desc = "Bazel rules for the packaging distributions",
        project_url = "https://github.com/bazelbuild/rules_pkg",
        version = "0.6.0",
        sha256 = "04535dbfbdf3ec839a2c578a0705a34e5a0bbfd4438b29e285b961e6e0b97ce1",
        strip_prefix = "rules_pkg-{version}",
        urls = ["https://github.com/bazelbuild/rules_pkg/archive/{version}.tar.gz"],
        use_category = ["build"],
        release_date = "2022-01-24",
    ),
    com_github_google_quiche = dict(
        project_name = "QUICHE",
        project_desc = "QUICHE (QUIC, HTTP/2, Etc) is Google‘s implementation of QUIC and related protocols",
        project_url = "https://github.com/google/quiche",
        version = "8864d08f60cf15467750d8843d864d66eafe0d5e",
        sha256 = "355ef1d08c02a2b3b2b8b52640c720ac5d5b9e83350c32851c8c0285b3a570d1",
        urls = ["https://github.com/google/quiche/archive/{version}.tar.gz"],
        strip_prefix = "quiche-{version}",
        use_category = ["dataplane_core"],
        release_date = "2022-04-11",
        cpe = "N/A",
    ),
    com_googlesource_googleurl = dict(
        project_name = "Chrome URL parsing library",
        project_desc = "Chrome URL parsing library",
        project_url = "https://quiche.googlesource.com/googleurl",
        # Static snapshot of https://quiche.googlesource.com/googleurl/+archive/9cdb1f4d1a365ebdbcbf179dadf7f8aa5ee802e7.tar.gz.
        version = "9cdb1f4d1a365ebdbcbf179dadf7f8aa5ee802e7",
        sha256 = "a1bc96169d34dcc1406ffb750deef3bc8718bd1f9069a2878838e1bd905de989",
        urls = ["https://storage.googleapis.com/quiche-envoy-integration/googleurl_{version}.tar.gz"],
        use_category = ["controlplane", "dataplane_core"],
        extensions = [],
        release_date = "2022-04-04",
        cpe = "N/A",
    ),
    com_google_cel_cpp = dict(
        project_name = "Common Expression Language (CEL) C++ library",
        project_desc = "Common Expression Language (CEL) C++ library",
        project_url = "https://opensource.google/projects/cel",
        version = "60c7aeabb4e6fa633b49c14d6c6fc8f0516761b9",
        sha256 = "7cb1e8ce293182e1d28321d4d6baecdacbc263cffcd9da1f7ffd25312611a329",
        strip_prefix = "cel-cpp-{version}",
        urls = ["https://github.com/google/cel-cpp/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = [
            "envoy.access_loggers.extension_filters.cel",
            "envoy.access_loggers.wasm",
            "envoy.bootstrap.wasm",
            "envoy.rate_limit_descriptors.expr",
            "envoy.filters.http.rbac",
            "envoy.filters.http.wasm",
            "envoy.filters.network.rbac",
            "envoy.filters.network.wasm",
            "envoy.stat_sinks.wasm",
            "envoy.rbac.matchers.upstream_ip_port",
        ],
        release_date = "2021-11-08",
        cpe = "N/A",
    ),
#    com_github_google_flatbuffers = dict(
#        project_name = "FlatBuffers",
#        project_desc = "Cross platform serialization library architected for maximum memory efficiency",
#        project_url = "https://github.com/google/flatbuffers",
#        version = "2.0.0",
#        sha256 = "9ddb9031798f4f8754d00fca2f1a68ecf9d0f83dfac7239af1311e4fd9a565c4",
#        strip_prefix = "flatbuffers-{version}",
#        urls = ["https://github.com/google/flatbuffers/archive/v{version}.tar.gz"],
#        use_category = ["dataplane_ext"],
#        extensions = [
#            "envoy.access_loggers.extension_filters.cel",
#            "envoy.access_loggers.wasm",
#            "envoy.bootstrap.wasm",
#            "envoy.rate_limit_descriptors.expr",
#            "envoy.filters.http.rbac",
#            "envoy.filters.http.wasm",
#            "envoy.filters.network.rbac",
#            "envoy.filters.network.wasm",
#            "envoy.stat_sinks.wasm",
#            "envoy.rbac.matchers.upstream_ip_port",
#        ],
#        release_date = "2021-05-10",
#        cpe = "cpe:2.3:a:google:flatbuffers:*",
#    ),
    com_googlesource_code_re2 = dict(
        project_name = "RE2",
        project_desc = "RE2, a regular expression library",
        project_url = "https://github.com/google/re2",
        version = "2022-04-01",
        sha256 = "1ae8ccfdb1066a731bba6ee0881baad5efd2cd661acd9569b689f2586e1a50e9",
        strip_prefix = "re2-{version}",
        urls = ["https://github.com/google/re2/archive/{version}.tar.gz"],
        use_category = ["controlplane", "dataplane_core"],
        release_date = "2022-03-31",
        cpe = "N/A",
    ),
    upb = dict(
        project_name = "upb",
        project_desc = "A small protobuf implementation in C (gRPC dependency)",
        project_url = "https://github.com/protocolbuffers/upb",
        version = "de76b31f9c56b28120580d53a6f8d7941fdb79eb",
        sha256 = "487d84ce85065ff89ccde1c1ac2ea1515d2be411306e4adf1be6861dc4a4a86b",
        release_date = "2020-12-29",
        strip_prefix = "upb-{version}",
        urls = ["https://github.com/protocolbuffers/upb/archive/{version}.tar.gz"],
        use_category = ["controlplane"],
        cpe = "N/A",
    ),
    rules_antlr = dict(
        project_name = "ANTLR Rules for Bazel",
        project_desc = "Bazel rules for ANTLR",
        project_url = "https://github.com/marcohu/rules_antlr",
        version = "3cc2f9502a54ceb7b79b37383316b23c4da66f9a",
        sha256 = "7249d1569293d9b239e23c65f6b4c81a07da921738bde0dfeb231ed98be40429",
        strip_prefix = "rules_antlr-{version}",
        urls = ["https://github.com/marcohu/rules_antlr/archive/{version}.tar.gz"],
        # ANTLR has a runtime component, so is not purely build.
        use_category = ["dataplane_ext"],
        extensions = [
            "envoy.access_loggers.extension_filters.cel",
            "envoy.access_loggers.wasm",
            "envoy.bootstrap.wasm",
            "envoy.rate_limit_descriptors.expr",
            "envoy.filters.http.wasm",
            "envoy.filters.network.wasm",
            "envoy.stat_sinks.wasm",
        ],
        release_date = "2019-06-21",
        cpe = "N/A",
    ),
    antlr4_runtimes = dict(
        project_name = "ANTLR v4",
        project_desc = "ANTLR (ANother Tool for Language Recognition) is a powerful parser generator for reading, processing, executing, or translating structured text or binary files",
        project_url = "https://github.com/antlr/antlr4",
        version = "4.7.2",
        sha256 = "46f5e1af5f4bd28ade55cb632f9a069656b31fc8c2408f9aa045f9b5f5caad64",
        strip_prefix = "antlr4-{version}",
        urls = ["https://github.com/antlr/antlr4/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = [
            "envoy.access_loggers.extension_filters.cel",
            "envoy.access_loggers.wasm",
            "envoy.bootstrap.wasm",
            "envoy.rate_limit_descriptors.expr",
            "envoy.filters.http.wasm",
            "envoy.filters.network.wasm",
            "envoy.stat_sinks.wasm",
        ],
        release_date = "2018-12-18",
        cpe = "N/A",
    ),
)
