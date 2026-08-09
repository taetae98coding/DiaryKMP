@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.compose.map

import org.w3c.dom.HTMLElement

internal external interface GoogleMapState : JsAny {
    fun attach(element: HTMLElement)

    fun release(element: HTMLElement)

    fun showSpot(
        latitude: Double,
        longitude: Double,
    )

    fun clearSpot()

    fun setPins(pinsJson: String)

    fun moveTo(
        latitude: Double,
        longitude: Double,
    )
}

internal fun googleMapOptions(): JsAny =
    js(
        """
        ({
            gestureHandling: 'greedy',
            zoomControl: true,
            mapTypeControl: true,
            scaleControl: true,
            streetViewControl: true,
            rotateControl: true,
            fullscreenControl: true,
            cameraControl: true,
            keyboardShortcuts: true,
            clickableIcons: true
        })
        """,
    )

@Suppress("UnusedParameter", "LongMethod")
internal fun GoogleMapState(
    options: JsAny,
    pinMarkerJson: String,
    isSpotSelectable: Boolean,
    isPinSelectable: Boolean,
    onCamera: (
        latitude: Double,
        longitude: Double,
        zoom: Double,
        south: Double,
        north: Double,
        west: Double,
        east: Double,
    ) -> Unit,
    onSpot: (latitude: Double, longitude: Double) -> Unit,
    onPin: (id: String) -> Unit,
): GoogleMapState =
    js(
        """
        (() => {
            const worldSize = 256;
            let currentElement;
            let map;
            let marker;
            let pinMarkers = [];
            let resizeObserver;
            let pendingSpot = null;
            let pendingPins = [];
            let pendingCenter = null;

            const pinMarker = JSON.parse(pinMarkerJson);
            const pinScale = pinMarker.size / pinMarker.viewport;

            const pinIcon = (pin) => {
                return {
                    path: pinMarker.path,
                    fillColor: pin.color,
                    fillOpacity: 1,
                    strokeWeight: 0,
                    scale: pinScale,
                    anchor: new google.maps.Point(pinMarker.viewport / 2, pinMarker.viewport),
                    labelOrigin: new google.maps.Point(
                        pinMarker.viewport / 2,
                        pinMarker.viewport + pinMarker.labelHeight / (2 * pinScale)
                    )
                };
            };

            const applyPins = () => {
                if (map === undefined) {
                    return;
                }
                pinMarkers.forEach((pinMarker) => {
                    pinMarker.setMap(null);
                });
                pinMarkers = pendingPins.map((pin) => {
                    const pinMarker = new google.maps.Marker({
                        position: { lat: pin.latitude, lng: pin.longitude },
                        map: map,
                        clickable: isPinSelectable,
                        icon: pinIcon(pin),
                        label: pin.label === "" ? null : { text: pin.label, fontSize: "12px" }
                    });
                    if (isPinSelectable) {
                        pinMarker.addListener('click', () => {
                            onPin(pin.id);
                        });
                    }
                    return pinMarker;
                });
            };

            const applySpot = () => {
                if (map === undefined) {
                    return;
                }
                if (pendingSpot === null) {
                    if (marker !== undefined) {
                        marker.setMap(null);
                        marker = undefined;
                    }
                    return;
                }
                const position = { lat: pendingSpot.latitude, lng: pendingSpot.longitude };
                if (marker === undefined) {
                    marker = new google.maps.Marker({ position: position, map: map });
                } else {
                    marker.setPosition(position);
                    marker.setMap(map);
                }
            };

            const applyCenter = () => {
                if (map === undefined || pendingCenter === null) {
                    return;
                }
                map.setCenter({ lat: pendingCenter.latitude, lng: pendingCenter.longitude });
                pendingCenter = null;
            };

            const dispose = () => {
                if (resizeObserver !== undefined) {
                    resizeObserver.disconnect();
                    resizeObserver = undefined;
                }
                if (marker !== undefined) {
                    marker.setMap(null);
                    marker = undefined;
                }
                pinMarkers.forEach((pinMarker) => {
                    pinMarker.setMap(null);
                });
                pinMarkers = [];
                map = undefined;
            };

            return {
                attach: async (newElement) => {
                    dispose();
                    currentElement = newElement;

                    if (window.diaryGoogleMapReady === undefined) {
                        return;
                    }
                    await window.diaryGoogleMapReady;
                    if (currentElement !== newElement) {
                        return;
                    }

                    resizeObserver = new ResizeObserver(() => {
                        if (currentElement !== newElement || map !== undefined) {
                            return;
                        }
                        if (newElement.clientWidth === 0 || newElement.clientHeight === 0) {
                            return;
                        }

                        const viewportSize = Math.max(newElement.clientWidth, newElement.clientHeight, worldSize);

                        map = new google.maps.Map(newElement, Object.assign({
                            center: { lat: 0, lng: 0 },
                            zoom: Math.ceil(Math.log2(viewportSize / worldSize))
                        }, options));
                        map.addListener('idle', () => {
                            if (map === undefined) {
                                return;
                            }
                            const center = map.getCenter();
                            const bounds = map.getBounds();
                            if (bounds) {
                                const boundsJson = bounds.toJSON();
                                onCamera(
                                    center.lat(), center.lng(), map.getZoom(),
                                    boundsJson.south, boundsJson.north, boundsJson.west, boundsJson.east
                                );
                            } else {
                                onCamera(center.lat(), center.lng(), map.getZoom(), NaN, NaN, NaN, NaN);
                            }
                        });
                        if (isSpotSelectable) {
                            map.addListener('click', (event) => {
                                onSpot(event.latLng.lat(), event.latLng.lng());
                            });
                        }
                        applySpot();
                        applyPins();
                        applyCenter();
                    });
                    resizeObserver.observe(newElement);
                },
                release: (releasedElement) => {
                    if (currentElement !== releasedElement) {
                        return;
                    }

                    dispose();
                    currentElement = undefined;
                },
                showSpot: (latitude, longitude) => {
                    pendingSpot = { latitude: latitude, longitude: longitude };
                    applySpot();
                },
                clearSpot: () => {
                    pendingSpot = null;
                    applySpot();
                },
                setPins: (pinsJson) => {
                    pendingPins = JSON.parse(pinsJson);
                    applyPins();
                },
                moveTo: (latitude, longitude) => {
                    pendingCenter = { latitude: latitude, longitude: longitude };
                    applyCenter();
                }
            };
        })()
        """,
    )
