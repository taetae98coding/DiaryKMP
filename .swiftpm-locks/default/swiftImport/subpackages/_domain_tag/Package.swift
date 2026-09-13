// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_tag",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_tag",
      type: .none,
      targets: ["_domain_tag"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_tag",
      dependencies: [
      ]
    )
  ]
)
