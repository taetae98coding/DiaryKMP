// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_search",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_data_search",
      type: .none,
      targets: ["_data_search"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_data_search",
      dependencies: [
      ]
    )
  ]
)
