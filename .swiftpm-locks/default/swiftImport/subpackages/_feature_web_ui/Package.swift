// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_feature_web_ui",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_feature_web_ui",
      type: .none,
      targets: ["_feature_web_ui"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_feature_web_ui",
      dependencies: [
      ]
    )
  ]
)
