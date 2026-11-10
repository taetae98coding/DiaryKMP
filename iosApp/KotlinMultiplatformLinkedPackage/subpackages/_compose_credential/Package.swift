// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_compose_credential",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_compose_credential",
      type: .none,
      targets: ["_compose_credential"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/google/GoogleSignIn-iOS.git",
      exact: "10.1.0"
    )
  ],
  targets: [
    .target(
      name: "_compose_credential",
      dependencies: [
        .product(
          name: "GoogleSignIn",
          package: "GoogleSignIn-iOS"
        )
      ]
    )
  ]
)
