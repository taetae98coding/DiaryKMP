// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_feature_login_ui",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_feature_login_ui",
      type: .none,
      targets: ["_feature_login_ui"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/google/GoogleSignIn-iOS.git",
      exact: "9.2.0"
    )
  ],
  targets: [
    .target(
      name: "_feature_login_ui",
      dependencies: [
        .product(
          name: "GoogleSignIn",
          package: "GoogleSignIn-iOS"
        )
      ]
    )
  ]
)
