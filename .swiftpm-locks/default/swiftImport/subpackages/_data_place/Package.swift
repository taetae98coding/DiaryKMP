// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_place",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_data_place",
      type: .none,
      targets: ["_data_place"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_data_place",
      dependencies: [
      ]
    )
  ]
)
