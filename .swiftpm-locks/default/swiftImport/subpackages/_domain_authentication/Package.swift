// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_authentication",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_authentication",
      type: .none,
      targets: ["_domain_authentication"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_authentication",
      dependencies: [
      ]
    )
  ]
)
