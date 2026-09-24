// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_core_fcm_impl",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_core_fcm_impl",
      type: .none,
      targets: ["_core_fcm_impl"]
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
      name: "_core_fcm_impl",
      dependencies: [
        .product(
          name: "FirebaseMessaging",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
