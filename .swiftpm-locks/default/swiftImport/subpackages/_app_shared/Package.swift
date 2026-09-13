// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_app_shared",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_app_shared",
      type: .none,
      targets: ["_app_shared"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_app_shared",
      dependencies: [
      ]
    )
  ]
)
