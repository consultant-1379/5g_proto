PROTOBUF_VERSION = "23.4"

REPOSITORY_LOCATIONS_SPEC = dict(
    boringssl = dict(
        project_name = "BoringSSL",
        project_desc = "Minimal OpenSSL fork",
        project_url = "https://github.com/google/boringssl",
        version = "88d7a40bd06a34da6ee0d985545755199d047258",
        sha256 = "1e759891e168c5957f2f4d519929e2b4cef9303b7cf2049601081f4fca95bf21",
        strip_prefix = "boringssl-{version}",
        urls = ["https://github.com/google/boringssl/archive/{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        release_date = "2023-02-14",
        cpe = "cpe:2.3:a:google:boringssl:*",
        license = "Mixed",
        license_url = "https://github.com/google/boringssl/blob/{version}/LICENSE",
    ),
    boringssl_fips = dict(
        project_name = "BoringSSL (FIPS)",
        project_desc = "FIPS compliant BoringSSL",
        project_url = "https://boringssl.googlesource.com/boringssl/+/master/crypto/fipsmodule/FIPS.md",
        version = "fips-20210429",
        sha256 = "a4d069ccef6f3c7bc0c68de82b91414f05cb817494cd1ab483dcf3368883c7c2",
        # urls = ["https://commondatastorage.googleapis.com/chromium-boringssl-fips/boringssl-853ca1ea1168dff08011e5d42d94609cc0ca2e27.tar.xz"],
        urls = ["https://github.com/google/boringssl/archive/{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        release_date = "2021-04-29",
        cpe = "cpe:2.3:a:google:boringssl:*",
    ),
    com_google_absl = dict(
        project_name = "Abseil",
        project_desc = "Open source collection of C++ libraries drawn from the most fundamental pieces of Google’s internal codebase",
        project_url = "https://abseil.io/",
        version = "c8b33b0191a2db8364cacf94b267ea8a3f20ad83",
        sha256 = "a7803eac00bf68eae1a84ee3b9fcf0c1173e8d9b89b2cee92c7b487ea65be2a9",
        strip_prefix = "abseil-cpp-{version}",
        urls = ["https://github.com/abseil/abseil-cpp/archive/{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2023-05-16",
        cpe = "N/A",
        license = "Apache-2.0",
        license_url = "https://github.com/abseil/abseil-cpp/blob/{version}/LICENSE",
    ),
    com_github_axboe_liburing = dict(
        project_name = "liburing",
        project_desc = "C helpers to set up and tear down io_uring instances",
        project_url = "https://github.com/axboe/liburing",
        version = "2.3",
        sha256 = "60b367dbdc6f2b0418a6e0cd203ee0049d9d629a36706fcf91dfb9428bae23c8",
        strip_prefix = "liburing-liburing-{version}",
        urls = ["https://github.com/axboe/liburing/archive/liburing-{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2022-10-26",
        cpe = "N/A",
    ),
    com_github_google_perfetto = dict(
        project_name = "Perfetto",
        project_desc = "Perfetto Tracing SDK",
        project_url = "https://perfetto.dev/",
        version = "36.1",
        sha256 = "b46145b6009dd7367ab12ef1e36a1656ec004674d3df167184a0ba6ceb384283",
        strip_prefix = "perfetto-{version}/sdk",
        urls = ["https://github.com/google/perfetto/archive/v{version}.tar.gz"],
        use_category = ["dataplane_core", "controlplane", "eric_sc_excluded"],
        release_date = "2023-07-06",
        cpe = "N/A",
        license = "Apache-2.0",
        license_url = "https://github.com/google/perfetto/blob/v{version}/LICENSE",
    ),
    com_github_c_ares_c_ares = dict(
        project_name = "c-ares",
        project_desc = "C library for asynchronous DNS requests",
        project_url = "https://c-ares.haxx.se/",
        version = "1.19.1",
        sha256 = "321700399b72ed0e037d0074c629e7741f6b2ec2dda92956abe3e9671d3e268e",
        strip_prefix = "c-ares-{version}",
        # urls = ["https://github.com/c-ares/c-ares/releases/download/cares-{underscore_version}/c-ares-{version}.tar.gz"],
        urls = ["https://c-ares.org/download/c-ares-{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2023-05-22",
        cpe = "cpe:2.3:a:c-ares_project:c-ares:*",
        license = "c-ares",
        license_url = "https://github.com/c-ares/c-ares/blob/cares-{underscore_version}/LICENSE.md",
    ),
    com_github_openhistogram_libcircllhist = dict(
        project_name = "libcircllhist",
        project_desc = "An implementation of OpenHistogram log-linear histograms",
        project_url = "https://github.com/openhistogram/libcircllhist",
        version = "39f9db724a81ba78f5d037f1cae79c5a07107c8e",
        sha256 = "fd2492f6cc1f8734f8f57be8c2e7f2907e94ee2a4c02445ce59c4241fece144b",
        strip_prefix = "libcircllhist-{version}",
        # urls = ["https://github.com/openhistogram/libcircllhist/archive/{version}.tar.gz"],
        urls = ["https://github.com/circonus-labs/libcircllhist/archive/{version}.tar.gz"],
        use_category = ["controlplane","observability_core","dataplane_core"],
        release_date = "2019-05-21",
        cpe = "N/A",
        license = "Apache-2.0",
        license_url = "https://github.com/openhistogram/libcircllhist/blob/{version}/LICENSE",
    ),
    com_github_cyan4973_xxhash = dict(
        project_name = "xxHash",
        project_desc = "Extremely fast hash algorithm",
        project_url = "https://github.com/Cyan4973/xxHash",
        version = "0.8.2",
        sha256 = "baee0c6afd4f03165de7a4e67988d16f0f2b257b51d0e3cb91909302a26a79c4",
        strip_prefix = "xxHash-{version}",
        urls = ["https://github.com/Cyan4973/xxHash/archive/v{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2023-07-21",
        cpe = "N/A",
        license = "BSD-2-Clause",
        license_url = "https://github.com/Cyan4973/xxHash/blob/v{version}/LICENSE",
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
        license = "MIT",
        license_url = "https://github.com/mirror/tclap/blob/v{version}/COPYING",
    ),
    com_github_fmtlib_fmt = dict(
        project_name = "fmt",
        project_desc = "{fmt} is an open-source formatting library providing a fast and safe alternative to C stdio and C++ iostreams",
        project_url = "https://fmt.dev",
        version = "9.1.0",
        sha256 = "cceb4cb9366e18a5742128cb3524ce5f50e88b476f1e54737a47ffdf4df4c996",
        strip_prefix = "fmt-{version}",
        # urls = ["https://github.com/fmtlib/fmt/releases/download/{version}/fmt-{version}.zip"],
        urls = ["https://github.com/fmtlib/fmt/archive/{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2022-08-27",
        cpe = "cpe:2.3:a:fmt:fmt:*",
        license = "fmt",
        license_url = "https://github.com/fmtlib/fmt/blob/{version}/LICENSE.rst",
    ),
    com_github_gabime_spdlog = dict(
        project_name = "spdlog",
        project_desc = "Very fast, header-only/compiled, C++ logging library",
        project_url = "https://github.com/gabime/spdlog",
        version = "1.12.0",
        sha256 = "4dccf2d10f410c1e2feaff89966bfc49a1abb29ef6f08246335b110e001e09a9",
        strip_prefix = "spdlog-{version}",
        urls = ["https://github.com/gabime/spdlog/archive/v{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2023-07-08",
        cpe = "N/A",
        license = "MIT",
        license_url = "https://github.com/gabime/spdlog/blob/v{version}/LICENSE",
    ),
    com_github_google_tcmalloc = dict(
        project_name = "tcmalloc",
        project_desc = "Fast, multi-threaded malloc implementation",
        project_url = "https://github.com/google/tcmalloc",
        version = "e33c7bc60415127c104006d3301c96902f98d42a",
        sha256 = "14a2c91b71d6719558768a79671408c9acd8284b418e80386c5888047e2c15aa",
        strip_prefix = "tcmalloc-{version}",
        urls = ["https://github.com/google/tcmalloc/archive/{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2022-10-24",
        cpe = "N/A",
        license = "Apache-2.0",
        license_url = "https://github.com/google/tcmalloc/blob/{version}/LICENSE",
    ),
    com_github_gperftools_gperftools = dict(
        project_name = "gperftools",
        project_desc = "tcmalloc and profiling libraries",
        project_url = "https://github.com/gperftools/gperftools",
        version = "2.10",
        sha256 = "83e3bfdd28b8bcf53222c3798d4d395d52dadbbae59e8730c4a6d31a9c3732d8",
        strip_prefix = "gperftools-{version}",
        # urls = ["https://github.com/gperftools/gperftools/releases/download/gperftools-{version}/gperftools-{version}.tar.gz"],
        urls = ["https://github.com/gperftools/gperftools/archive/gperftools-{version}.tar.gz"],
        release_date = "2022-05-31",
        use_category = ["dataplane_core","controlplane"],
        cpe = "cpe:2.3:a:gperftools_project:gperftools:*",
        license = "BSD-3-Clause",
        license_url = "https://github.com/gperftools/gperftools/blob/gperftools-{version}/COPYING",
    ),
    com_github_grpc_grpc = dict(
        project_name = "gRPC",
        project_desc = "gRPC C core library",
        project_url = "https://grpc.io",
        version = "1.56.2",
        sha256 = "931f07db9d48cff6a6007c1033ba6d691fe655bea2765444bc1ad974dfc840aa",
        strip_prefix = "grpc-{version}",
        urls = ["https://github.com/grpc/grpc/archive/v{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2023-07-14",
        cpe = "cpe:2.3:a:grpc:grpc:*",
        license = "Apache-2.0",
        license_url = "https://github.com/grpc/grpc/blob/v{version}/LICENSE",
    ),
    com_github_nghttp2_nghttp2 = dict(
        project_name = "Nghttp2",
        project_desc = "Implementation of HTTP/2 and its header compression algorithm HPACK in C",
        project_url = "https://nghttp2.org",
        version = "1.55.1",
        sha256 = "e12fddb65ae3218b4edc083501519379928eba153e71a1673b185570f08beb96",
        strip_prefix = "nghttp2-{version}",
        # urls = ["https://github.com/nghttp2/nghttp2/releases/download/v{version}/nghttp2-{version}.tar.gz"],
        urls = ["https://github.com/nghttp2/nghttp2/archive/v{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        release_date = "2023-07-14",
        cpe = "cpe:2.3:a:nghttp2:nghttp2:*",
        license = "MIT",
        license_url = "https://github.com/nghttp2/nghttp2/blob/v{version}/LICENSE",
    ),
    com_github_libevent_libevent = dict(
        project_name = "libevent",
        project_desc = "Event notification library",
        project_url = "https://libevent.org",
        version = "62c152d9a7cd264b993dad730c4163c6ede2e0a3",
        sha256 = "4c80e5fe044ce5f8055b20a2f141ee32ec2614000f3e95d2aa81611a4c8f5213",
        strip_prefix = "libevent-{version}",
        urls = ["https://github.com/libevent/libevent/archive/{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2020-07-28",
        cpe = "cpe:2.3:a:libevent_project:libevent:*",
        license = "BSD-3-Clause",
        license_url = "https://github.com/libevent/libevent/blob/{version}/LICENSE",
    ),
    net_zlib = dict(
        project_name = "zlib",
        project_desc = "zlib compression library",
        project_url = "https://zlib.net",
        version = "1.2.13",
        sha256 = "1525952a0a567581792613a9723333d7f8cc20b87a81f920fb8bc7e3f2251428",
        strip_prefix = "zlib-{version}",
        urls = ["https://github.com/madler/zlib/archive/v{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        release_date = "2022-10-14",
        cpe = "cpe:2.3:a:gnu:zlib:*",
        license = "zlib",
        license_url = "https://github.com/madler/zlib/blob/v{version}/zlib.h",
    ),
    com_github_zlib_ng_zlib_ng = dict(
        project_name = "zlib-ng",
        project_desc = "zlib fork (higher performance)",
        project_url = "https://github.com/zlib-ng/zlib-ng",
        version = "2.0.7",
        sha256 = "6c0853bb27738b811f2b4d4af095323c3d5ce36ceed6b50e5f773204fb8f7200",
        strip_prefix = "zlib-ng-{version}",
        urls = ["https://github.com/zlib-ng/zlib-ng/archive/{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        release_date = "2023-03-17",
        cpe = "N/A",
        license = "zlib",
        license_url = "https://github.com/zlib-ng/zlib-ng/blob/{version}/LICENSE.md",
    ),
    com_github_jbeder_yaml_cpp = dict(
        project_name = "yaml-cpp",
        project_desc = "YAML parser and emitter in C++ matching the YAML 1.2 spec",
        project_url = "https://github.com/jbeder/yaml-cpp",
        version = "0.8.0",
        sha256 = "fbe74bbdcee21d656715688706da3c8becfd946d92cd44705cc6098bb23b3a16",
        strip_prefix = "yaml-cpp-{version}",
        urls = ["https://github.com/jbeder/yaml-cpp/archive/{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        release_date = "2023-08-10",
        cpe = "cpe:2.3:a:yaml-cpp_project:yaml-cpp:*",
        license = "MIT",
        license_url = "https://github.com/jbeder/yaml-cpp/blob/{version}/LICENSE",
    ),
    com_github_google_jwt_verify = dict(
        project_name = "jwt_verify_lib",
        project_desc = "JWT verification library for C++",
        project_url = "https://github.com/google/jwt_verify_lib",
        version = "c29ba4bdab2cc9a7b4d80d1d3ebff3bf5b9bf6e2",
        sha256 = "5851ab1857edf46b31dc298fba984e1b7638f80a58f88a84a83402540643a99f",
        strip_prefix = "jwt_verify_lib-{version}",
        urls = ["https://github.com/google/jwt_verify_lib/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = ["envoy.filters.http.jwt_authn","envoy.filters.http.gcp_authn"],
        release_date = "2022-11-04",
        cpe = "N/A",
        license = "Apache-2.0",
        license_url = "https://github.com/google/jwt_verify_lib/blob/{version}/LICENSE",
    ),
    com_github_nlohmann_json = dict(
        project_name = "nlohmann JSON",
        project_desc = "Fast JSON parser/generator for C++",
        project_url = "https://nlohmann.github.io/json",
        version = "3.11.2",
        sha256 = "d69f9deb6a75e2580465c6c4c5111b89c4dc2fa94e3a85fcd2ffcd9a143d9273",
        strip_prefix = "json-{version}",
        urls = ["https://github.com/nlohmann/json/archive/v{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        release_date = "2022-08-12",
        cpe = "cpe:2.3:a:json-for-modern-cpp_project:json-for-modern-cpp:*",
        license = "MIT",
        license_url = "https://github.com/nlohmann/json/blob/v{version}/LICENSE.MIT",
    ),
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
        license = "MIT",
        license_url = "https://github.com/ncopa/su-exec/blob/{version}/LICENSE",
    ),
    com_google_protobuf = dict(
        project_name = "Protocol Buffers",
        project_desc = "Language-neutral, platform-neutral extensible mechanism for serializing structured data",
        project_url = "https://developers.google.com/protocol-buffers",
        version = "23.4",
        sha256 = "a700a49470d301f1190a487a923b5095bf60f08f4ae4cac9f5f7c36883d17971",
        strip_prefix = "protobuf-{version}",
        #urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{version}/protobuf-{version}.tar.gz"],
        urls = ["https://github.com/protocolbuffers/protobuf/archive/v{version}.tar.gz"],
        use_category = ["dataplane_core","controlplane"],
        release_date = "2023-07-06",
        cpe = "cpe:2.3:a:google:protobuf:*",
        license = "Protocol Buffers",
        license_url = "https://github.com/protocolbuffers/protobuf/blob/v{version}/LICENSE",
    ),
    io_bazel_rules_go = dict(
        project_name = "Go rules for Bazel",
        project_desc = "Bazel rules for the Go language",
        project_url = "https://github.com/bazelbuild/rules_go",
        version = "0.39.1",
        sha256 = "6dc2da7ab4cf5d7bfc7c949776b1b7c733f05e56edc4bcd9022bb249d2e2a996",
        # urls = ["https://github.com/bazelbuild/rules_go/releases/download/v{version}/rules_go-v{version}.zip"],
        urls = ["https://github.com/bazelbuild/rules_go/archive/refs/tags/v{version}.tar.gz"],
        use_category = ["build","api"],
        release_date = "2023-04-20",
        implied_untracked_deps = ["com_github_golang_protobuf","io_bazel_rules_nogo","org_golang_google_protobuf","org_golang_x_tools"],
        license = "Apache-2.0",
        license_url = "https://github.com/bazelbuild/rules_go/blob/v{version}/LICENSE.txt",
    ),
    rules_foreign_cc = dict(
        project_name = "Rules for using foreign build systems in Bazel",
        project_desc = "Rules for using foreign build systems in Bazel",
        project_url = "https://github.com/bazelbuild/rules_foreign_cc",
        version = "0.9.0",
        sha256 = "2a4d07cd64b0719b39a7c12218a3e507672b82a97b98c6a89d38565894cf7c51",
        strip_prefix = "rules_foreign_cc-{version}",
        urls = ["https://github.com/bazelbuild/rules_foreign_cc/archive/{version}.tar.gz"],
        release_date = "2022-08-02",
        use_category = ["build","dataplane_core","controlplane"],
        license = "Apache-2.0",
        license_url = "https://github.com/bazelbuild/rules_foreign_cc/blob/{version}/LICENSE",
    ),
    com_github_google_quiche = dict(
        project_name = "QUICHE",
        project_desc = "QUICHE (QUIC, HTTP/2, Etc) is Google‘s implementation of QUIC and related protocols",
        project_url = "https://github.com/google/quiche",
        version = "92faee243386c6234f39ab5f3debbbd480cfcff6",
        sha256 = "1e7e5c08c4b00dccc1d41a5db9ffe856db6d4174149f9d32561b07fead532229",
        urls = ["https://github.com/google/quiche/archive/{version}.tar.gz"],
        strip_prefix = "quiche-{version}",
        use_category = ["controlplane","dataplane_core"],
        release_date = "2023-10-11",
        cpe = "N/A",
        license = "BSD-3-Clause",
        license_url = "https://github.com/google/quiche/blob/{version}/LICENSE",
    ),
    com_googlesource_googleurl = dict(
        project_name = "Chrome URL parsing library",
        project_desc = "Chrome URL parsing library",
        project_url = "https://quiche.googlesource.com/googleurl",
        version = "dd4080fec0b443296c0ed0036e1e776df8813aa7",
        sha256 = "59f14d4fb373083b9dc8d389f16bbb817b5f936d1d436aa67e16eb6936028a51",
        urls = ["https://storage.googleapis.com/quiche-envoy-integration/{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        extensions = [],
        release_date = "2022-11-03",
        cpe = "N/A",
        license = "googleurl",
        license_url = "https://quiche.googlesource.com/googleurl/+/{version}/LICENSE",
    ),
    com_google_cel_cpp = dict(
        project_name = "Common Expression Language (CEL) C++ library",
        project_desc = "Common Expression Language (CEL) C++ library",
        project_url = "https://opensource.google/projects/cel",
        version = "da0aba702f44a41ec6d2eb4bbf6a9f01efc2746d",
        sha256 = "d62b93fd07c6151749e83855157f3f2778d62c168318f9c40dfcfe1c336c496f",
        strip_prefix = "cel-cpp-{version}",
        urls = ["https://github.com/google/cel-cpp/archive/{version}.tar.gz"],
        use_category = ["dataplane_ext"],
        extensions = ["envoy.access_loggers.extension_filters.cel","envoy.access_loggers.wasm","envoy.bootstrap.wasm","envoy.rate_limit_descriptors.expr","envoy.filters.http.rate_limit_quota","envoy.filters.http.rbac","envoy.filters.http.wasm","envoy.filters.network.rbac","envoy.filters.network.wasm","envoy.stat_sinks.wasm","envoy.rbac.matchers.upstream_ip_port","envoy.formatter.cel","envoy.matching.inputs.cel_data_input","envoy.matching.matchers.cel_matcher"],
        release_date = "2023-03-08",
        cpe = "N/A",
    ),
    com_github_google_flatbuffers = dict(
        project_name = "FlatBuffers",
        project_desc = "FlatBuffers is a cross platform serialization library architected for maximum memory efficiency",
        project_url = "https://github.com/google/flatbuffers",
        version = "23.3.3",
        sha256 = "8aff985da30aaab37edf8e5b02fda33ed4cbdd962699a8e2af98fdef306f4e4d",
        strip_prefix = "flatbuffers-{version}",
        # urls = ["https://github.com/google/flatbuffers/archive/v{version}.tar.gz"],
        urls = ["https://github.com/google/flatbuffers/archive/refs/tags/v{version}.zip"],
        use_category = ["dataplane_ext"],
        extensions = ["envoy.access_loggers.extension_filters.cel","envoy.access_loggers.wasm","envoy.formatter.cel","envoy.bootstrap.wasm","envoy.rate_limit_descriptors.expr","envoy.filters.http.rate_limit_quota","envoy.filters.http.rbac","envoy.filters.http.wasm","envoy.filters.network.rbac","envoy.filters.network.wasm","envoy.stat_sinks.wasm","envoy.rbac.matchers.upstream_ip_port","envoy.matching.inputs.cel_data_input","envoy.matching.matchers.cel_matcher"],
        release_date = "2023-03-03",
        cpe = "cpe:2.3:a:google:flatbuffers:*",
        license = "Apache-2.0",
        license_url = "https://github.com/google/flatbuffers/blob/v{version}/LICENSE",
    ),
    com_googlesource_code_re2 = dict(
        project_name = "RE2",
        project_desc = "RE2, a regular expression library",
        project_url = "https://github.com/google/re2",
        version = "2023-09-01",
        sha256 = "5bb6875ae1cd1e9fedde98018c346db7260655f86fdb8837e3075103acd3649b",
        strip_prefix = "re2-{version}",
        # urls = ["https://github.com/google/re2/archive/{version}.tar.gz"],
        urls = ["https://github.com/google/re2/archive/refs/tags/{version}.tar.gz"],
        use_category = ["controlplane","dataplane_core"],
        release_date = "2023-08-31",
        cpe = "N/A",
        license = "BSD-3-Clause",
        license_url = "https://github.com/google/re2/blob/{version}/LICENSE",
    ),
    upb = dict(
        project_name = "upb",
        project_desc = "A small protobuf implementation in C (gRPC dependency)",
        project_url = "https://github.com/protocolbuffers/upb",
        version = "e074c038c35e781a1876f8eb52b14f822ae2db66",
        sha256 = "8608c15b5612c6154d4ee0c23910afe6c283985e1d368ea71704dcd8684135d4",
        release_date = "2023-07-21",
        strip_prefix = "upb-{version}",
        urls = ["https://github.com/protocolbuffers/upb/archive/{version}.tar.gz"],
        use_category = ["controlplane"],
        cpe = "N/A",
        license = "upb",
        license_url = "https://github.com/protocolbuffers/upb/blob/{version}/LICENSE",
    ),
    rules_license = dict(
        project_name = "rules_license",
        project_desc = "Bazel rules for checking open source licenses",
        project_url = "https://github.com/bazelbuild/rules_license",
        version = "0.0.7",
        sha256 = "4531deccb913639c30e5c7512a054d5d875698daeb75d8cf90f284375fe7c360",
        # urls = ["https://github.com/bazelbuild/rules_license/releases/download/{version}/rules_license-{version}.tar.gz"],
        urls = ["https://github.com/bazelbuild/rules_license/archive/{version}.tar.gz"],
        use_category = ["build","dataplane_core","controlplane"],
        release_date = "2023-06-16",
        cpe = "N/A",
        license = "Apache-2.0",
        license_url = "https://github.com/bazelbuild/rules_license/blob/{version}/LICENSE",
    ),
    utf8_range = dict(
        project_name = "utf8_range",
        project_desc = "Fast UTF-8 validation with Range algorithm (NEON+SSE4+AVX2)",
        project_url = "https://github.com/protocolbuffers/utf8_range",
        version = "d863bc33e15cba6d873c878dcca9e6fe52b2f8cb",
        sha256 = "c56f0a8c562050e6523a3095cf5610d19c190cd99bac622cc3e5754be51aaa7b",
        strip_prefix = "utf8_range-{version}",
        urls = ["https://github.com/protocolbuffers/utf8_range/archive/{version}.tar.gz"],
        use_category = ["build","dataplane_core","controlplane"],
        release_date = "2023-05-26",
        cpe = "N/A",
        license = "MIT",
        license_url = "https://github.com/protocolbuffers/utf8_range/blob/{version}/LICENSE",
    ),
    com_github_maxmind_libmaxminddb = dict(
        project_name = "maxmind_libmaxminddb",
        project_desc = "C library for reading MaxMind DB files",
        project_url = "https://github.com/maxmind/libmaxminddb",
        version = "1.7.1",
        sha256 = "e8414f0dedcecbc1f6c31cb65cd81650952ab0677a4d8c49cab603b3b8fb083e",
        strip_prefix = "libmaxminddb-{version}",
        urls = ["https://github.com/maxmind/libmaxminddb/releases/download/{version}/libmaxminddb-{version}.tar.gz"],
        use_category = ["dataplane_ext", "eric_sc_excluded"],
        extensions = ["envoy.geoip_providers.maxmind"],
        release_date = "2022-09-30",
        cpe = "cpe:2.3:a:maxmind:libmaxminddb:*",
        license = "Apache-2.0",
        license_url = "https://github.com/maxmind/libmaxminddb/blob/{version}/LICENSE",
    ),
    com_google_protobuf_protoc_linux_aarch_64 = dict(
        project_name = "Protocol Buffers (protoc) linux_aarch_64",
        project_desc = "Protoc compiler for protobuf (linux_aarch_64)",
        project_url = "https://developers.google.com/protocol-buffers",
        version = "23.4",
        sha256 = "1c7750b6e038305b5a7fc3d0cda1ebefdf106a4f30a787bf826ed2fc47c3967d",
        urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{version}/protoc-{version}-linux-aarch_64.zip"],
# FOSS for protocolbuffers already included earlier, excluding it here (eedrak)
        use_category = ["dataplane_core", "controlplane", "eric_sc_excluded"],
        release_date = "2023-07-06",
        cpe = "N/A",
        license = "Protocol Buffers",
        license_url = "https://github.com/protocolbuffers/protobuf/blob/v{version}/LICENSE",
    ),
    com_google_protobuf_protoc_linux_x86_64 = dict(
        project_name = "Protocol Buffers (protoc) linux_x86_64",
        project_desc = "Protoc compiler for protobuf (linux_x86_64)",
        project_url = "https://developers.google.com/protocol-buffers",
        version = "23.4",
        sha256 = "0502f286ac9ed860b629a7965a14527b1f2dd131e4283fa23c2d7f184672aa9a",
        urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{version}/protoc-{version}-linux-x86_64.zip"],
# FOSS for protocolbuffers already included earlier, excluding it here (eedrak)
        use_category = ["dataplane_core", "controlplane", "eric_sc_excluded"],
        release_date = "2023-07-06",
        cpe = "N/A",
        license = "Protocol Buffers",
        license_url = "https://github.com/protocolbuffers/protobuf/blob/v{version}/LICENSE",
    ),
    com_google_protobuf_protoc_osx_aarch_64 = dict(
        project_name = "Protocol Buffers (protoc) osx_aarch_64",
        project_desc = "Protoc compiler for protobuf (osx_aarch_64)",
        project_url = "https://developers.google.com/protocol-buffers",
        version = "23.4",
        sha256 = "8c7afae8626b6811e7b5897d16d940c2dbf50b1e135ed958a01db6566bdda726",
        urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{version}/protoc-{version}-osx-aarch_64.zip"],
# FOSS for protocolbuffers already included earlier, excluding it here (eedrak)
        use_category = ["dataplane_core", "controlplane", "eric_sc_excluded"],
        release_date = "2023-07-06",
        cpe = "N/A",
        license = "Protocol Buffers",
        license_url = "https://github.com/protocolbuffers/protobuf/blob/v{version}/LICENSE",
    ),
    com_google_protobuf_protoc_osx_x86_64 = dict(
        project_name = "Protocol Buffers (protoc) osx_x86_64",
        project_desc = "Protoc compiler for protobuf (osx_x86_64)",
        project_url = "https://developers.google.com/protocol-buffers",
        version = "23.4",
        sha256 = "07e5fdcf1b0708d3367dc5e6eb8d135de7e407d75316c93155cfd8ab362eec80",
        urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{version}/protoc-{version}-osx-x86_64.zip"],
# FOSS for protocolbuffers already included earlier, excluding it here (eedrak)
        use_category = ["dataplane_core", "controlplane", "eric_sc_excluded"],
        release_date = "2023-07-06",
        cpe = "N/A",
        license = "Protocol Buffers",
        license_url = "https://github.com/protocolbuffers/protobuf/blob/v{version}/LICENSE",
    ),
    com_google_protobuf_protoc_win64 = dict(
        project_name = "Protocol Buffers (protoc) win64",
        project_desc = "Protoc compiler for protobuf (win64)",
        project_url = "https://developers.google.com/protocol-buffers",
        version = "23.4",
        sha256 = "a309c39442fb75f0db343cb22c111a00f91cdf0767f332e170644b9378e2bcc6",
        urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{version}/protoc-{version}-win64.zip"],
# FOSS for protocolbuffers already included earlier, excluding it here (eedrak)
        use_category = ["dataplane_core", "controlplane", "eric_sc_excluded"],
        release_date = "2023-07-06",
        cpe = "N/A",
        license = "Protocol Buffers",
        license_url = "https://github.com/protocolbuffers/protobuf/blob/v{version}/LICENSE",
    ),
)
