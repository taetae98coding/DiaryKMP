// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain_weather",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_domain_weather",
      type: .none,
      targets: ["_domain_weather"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain_weather",
      dependencies: [
      ]
    )
  ]
)
