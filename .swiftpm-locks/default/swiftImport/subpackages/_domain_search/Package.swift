// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_search",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_search",
      type: .none,
      targets: ["_domain_search"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_search",
      dependencies: [
      ]
    )
  ]
)
