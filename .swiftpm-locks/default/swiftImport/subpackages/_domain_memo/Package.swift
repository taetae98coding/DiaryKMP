// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_memo",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_memo",
      type: .none,
      targets: ["_domain_memo"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_memo",
      dependencies: [
      ]
    )
  ]
)
