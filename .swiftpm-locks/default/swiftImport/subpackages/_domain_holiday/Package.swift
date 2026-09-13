// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_holiday",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_holiday",
      type: .none,
      targets: ["_domain_holiday"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_holiday",
      dependencies: [
      ]
    )
  ]
)
