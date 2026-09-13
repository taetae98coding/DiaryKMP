// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_place",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_place",
      type: .none,
      targets: ["_domain_place"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_place",
      dependencies: [
      ]
    )
  ]
)
