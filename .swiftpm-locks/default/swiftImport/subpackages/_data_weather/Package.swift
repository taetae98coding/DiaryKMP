// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_weather",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_data_weather",
      type: .none,
      targets: ["_data_weather"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_data_weather",
      dependencies: [
      ]
    )
  ]
)
