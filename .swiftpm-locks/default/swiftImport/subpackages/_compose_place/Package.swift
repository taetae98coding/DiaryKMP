// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_compose_place",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_compose_place",
      type: .none,
      targets: ["_compose_place"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_compose_place",
      dependencies: [
      ]
    )
  ]
)
