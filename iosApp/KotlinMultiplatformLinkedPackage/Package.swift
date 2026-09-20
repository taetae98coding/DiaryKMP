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
    .package(path: "subpackages/_feature_login_ui"),
    .package(path: "subpackages/_logger_analytics_impl"),
    .package(path: "subpackages/_logger_crashlytics_impl"),
    .package(path: "subpackages/_compose_map")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_feature_login_ui", package: "_feature_login_ui"),
        .product(name: "_logger_analytics_impl", package: "_logger_analytics_impl"),
        .product(name: "_logger_crashlytics_impl", package: "_logger_crashlytics_impl"),
        .product(name: "_compose_map", package: "_compose_map")
      ]
    )
  ]
)
