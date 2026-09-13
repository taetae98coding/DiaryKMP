// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_account",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_account",
      type: .none,
      targets: ["_domain_account"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_account",
      dependencies: [
      ]
    )
  ]
)
