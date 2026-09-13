// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_app_ios",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_app_ios",
      type: .none,
      targets: ["_app_ios"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_app_ios",
      dependencies: [
      ]
    )
  ]
)
