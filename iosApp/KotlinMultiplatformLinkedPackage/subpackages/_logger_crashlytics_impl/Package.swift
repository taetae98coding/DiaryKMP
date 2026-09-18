// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_logger_crashlytics_impl",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_logger_crashlytics_impl",
      type: .none,
      targets: ["_logger_crashlytics_impl"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/firebase/firebase-ios-sdk.git",
      exact: "12.19.1"
    )
  ],
  targets: [
    .target(
      name: "_logger_crashlytics_impl",
      dependencies: [
        .product(
          name: "FirebaseCrashlytics",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
