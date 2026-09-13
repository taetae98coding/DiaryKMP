// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_web",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_web",
      type: .none,
      targets: ["_domain_web"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_web",
      dependencies: [
      ]
    )
  ]
)
