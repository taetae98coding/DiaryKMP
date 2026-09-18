// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_compose_map",
  platforms: [
    .iOS("26.5")
  ],
  products: [
    .library(
      name: "_compose_map",
      type: .none,
      targets: ["_compose_map"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/navermaps/SPM-NMapsMap.git",
      exact: "3.24.0"
    ),
    .package(
      url: "https://github.com/googlemaps/ios-maps-sdk.git",
      exact: "11.1.0"
    )
  ],
  targets: [
    .target(
      name: "_compose_map",
      dependencies: [
        .product(
          name: "NMapsMap",
          package: "SPM-NMapsMap"
        ),
        .product(
          name: "GoogleMaps",
          package: "ios-maps-sdk"
        )
      ]
    )
  ]
)
