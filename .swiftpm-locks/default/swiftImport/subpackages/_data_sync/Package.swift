// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_sync",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_data_sync",
      type: .none,
      targets: ["_data_sync"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_data_sync",
      dependencies: [
      ]
    )
  ]
)
