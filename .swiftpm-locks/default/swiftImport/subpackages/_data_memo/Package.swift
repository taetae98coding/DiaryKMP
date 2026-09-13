// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_data_memo",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_data_memo",
      type: .none,
      targets: ["_data_memo"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_data_memo",
      dependencies: [
      ]
    )
  ]
)
