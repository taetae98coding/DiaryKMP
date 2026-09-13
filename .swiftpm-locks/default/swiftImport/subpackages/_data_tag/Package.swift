// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_tag",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_data_tag",
      type: .none,
      targets: ["_data_tag"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_data_tag",
      dependencies: [
      ]
    )
  ]
)
