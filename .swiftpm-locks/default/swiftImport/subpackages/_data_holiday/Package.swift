// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_holiday",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_data_holiday",
      type: .none,
      targets: ["_data_holiday"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_data_holiday",
      dependencies: [
      ]
    )
  ]
)
