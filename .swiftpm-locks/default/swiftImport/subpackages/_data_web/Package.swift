// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_web",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_data_web",
      type: .none,
      targets: ["_data_web"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_data_web",
      dependencies: [
      ]
    )
  ]
)
