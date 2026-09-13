// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_sync",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_sync",
      type: .none,
      targets: ["_domain_sync"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_sync",
      dependencies: [
      ]
    )
  ]
)
