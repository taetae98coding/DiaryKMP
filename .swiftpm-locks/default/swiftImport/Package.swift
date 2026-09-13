// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/_app_ios"),
    .package(path: "subpackages/_app_shared"),
    .package(path: "subpackages/_compose_map"),
    .package(path: "subpackages/_compose_place"),
    .package(path: "subpackages/_feature_login_ui"),
    .package(path: "subpackages/_feature_memo_ui"),
    .package(path: "subpackages/_feature_place_ui"),
    .package(path: "subpackages/_feature_search_ui"),
    .package(path: "subpackages/_feature_tag_ui"),
    .package(path: "subpackages/_logger_crashlytics_impl")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_app_ios", package: "_app_ios"),
        .product(name: "_app_shared", package: "_app_shared"),
        .product(name: "_compose_map", package: "_compose_map"),
        .product(name: "_compose_place", package: "_compose_place"),
        .product(name: "_feature_login_ui", package: "_feature_login_ui"),
        .product(name: "_feature_memo_ui", package: "_feature_memo_ui"),
        .product(name: "_feature_place_ui", package: "_feature_place_ui"),
        .product(name: "_feature_search_ui", package: "_feature_search_ui"),
        .product(name: "_feature_tag_ui", package: "_feature_tag_ui"),
        .product(name: "_logger_crashlytics_impl", package: "_logger_crashlytics_impl")
      ]
    )
  ]
)
