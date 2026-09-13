// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_core_work_impl",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_core_work_impl",
      type: .none,
      targets: ["_core_work_impl"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_core_work_impl",
      dependencies: [
      ]
    )
  ]
)
