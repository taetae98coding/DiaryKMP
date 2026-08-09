@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.compose.map

import org.w3c.dom.HTMLElement

internal external interface NaverMapState : JsAny {
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

internal fun naverMapOptions(): JsAny =
    js(
        """
        ({
            zoomControl: true,
            scaleControl: true,
            mapTypeControl: true,
            mapDataControl: true,
            logoControl: true,
            draggable: true,
            pinchZoom: true,
            scrollWheel: true,
            keyboardShortcuts: true,
            tileTransition: true,
            disableDoubleClickZoom: false,
            disableDoubleTapZoom: false,
            disableTwoFingerTapZoom: false,
            disableKineticPan: false
        })
        """,
    )

// js 본문은 하나의 문자열이어서 나눌 수 없으므로 함수 길이 검사에서 제외한다.
@Suppress("UnusedParameter", "LongMethod")
internal fun NaverMapState(
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
): NaverMapState =
    js(
        """
        (() => {
            let currentElement;
            let map;
            let marker;
            let pinMarkers = [];
            let resizeObserver;
            let pendingSpot = null;
            let pendingPins = [];
            let pendingCenter = null;

            const postCamera = () => {
                if (map === undefined) {
                    return;
                }
                const center = map.getCenter();
                const bounds = map.getBounds();
                if (bounds) {
                    onCamera(
                        center.lat(), center.lng(), map.getZoom(),
                        bounds.south(), bounds.north(), bounds.west(), bounds.east()
                    );
                } else {
                    onCamera(center.lat(), center.lng(), map.getZoom(), NaN, NaN, NaN, NaN);
                }
            };

            const pinMarker = JSON.parse(pinMarkerJson);

            const pinContent = (pin) => {
                const root = document.createElement("div");
                root.style.cssText = "position:relative;width:0;height:0;";
                const content = document.createElement("div");
                content.style.cssText = "position:absolute;left:0;top:-" + pinMarker.size + "px;transform:translateX(-50%);display:flex;flex-direction:column;align-items:center;";
                content.innerHTML =
                    '<svg width="' + pinMarker.size + '" height="' + pinMarker.size + '" viewBox="0 0 ' + pinMarker.viewport + ' ' + pinMarker.viewport + '">' +
                    '<path d="' + pinMarker.path + '"/>' +
                    '</svg>';
                content.querySelector("path").setAttribute("fill", pin.color);
                const label = document.createElement("span");
                label.style.cssText = "font-size:12px;line-height:" + pinMarker.labelHeight + "px;white-space:nowrap;color:#000000;background:rgba(255,255,255,0.8);padding:0 2px;border-radius:2px;";
                label.textContent = pin.label;
                content.appendChild(label);
                root.appendChild(content);
                return root;
            };

            const applyPins = () => {
                if (map === undefined) {
                    return;
                }
                pinMarkers.forEach((pinMarker) => {
                    pinMarker.setMap(null);
                });
                pinMarkers = pendingPins.map((pin) => {
                    const pinMarker = new naver.maps.Marker({
                        position: new naver.maps.LatLng(pin.latitude, pin.longitude),
                        map: map,
                        clickable: isPinSelectable,
                        icon: {
                            content: pinContent(pin),
                            anchor: new naver.maps.Point(0, 0)
                        }
                    });
                    if (isPinSelectable) {
                        naver.maps.Event.addListener(pinMarker, 'click', () => {
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
                const position = new naver.maps.LatLng(pendingSpot.latitude, pendingSpot.longitude);
                if (marker === undefined) {
                    marker = new naver.maps.Marker({ position: position, map: map });
                } else {
                    marker.setPosition(position);
                    marker.setMap(map);
                }
            };

            const applyCenter = () => {
                if (map === undefined || pendingCenter === null) {
                    return;
                }
                map.setCenter(new naver.maps.LatLng(pendingCenter.latitude, pendingCenter.longitude));
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
                if (map !== undefined) {
                    map.destroy();
                    map = undefined;
                }
            };

            return {
                attach: async (newElement) => {
                    dispose();
                    currentElement = newElement;

                    if (window.diaryNaverMapReady === undefined) {
                        return;
                    }
                    await window.diaryNaverMapReady;
                    if (currentElement !== newElement) {
                        return;
                    }

                    resizeObserver = new ResizeObserver(() => {
                        if (currentElement !== newElement) {
                            return;
                        }
                        if (newElement.clientWidth === 0 || newElement.clientHeight === 0) {
                            return;
                        }
                        if (map === undefined) {
                            map = new naver.maps.Map(newElement, options);
                            naver.maps.Event.addListener(map, 'idle', postCamera);
                            // 네이버 지도의 idle은 이동·확대·축소가 끝났을 때만 발생해 최초 로드에서는
                            // 보이는 영역이 전달되지 않으므로, 초기화가 끝나면 한 번 직접 알린다.
                            naver.maps.Event.once(map, 'init', postCamera);
                            if (isSpotSelectable) {
                                naver.maps.Event.addListener(map, 'click', (event) => {
                                    onSpot(event.coord.lat(), event.coord.lng());
                                });
                            }
                            applySpot();
                            applyPins();
                            applyCenter();
                            postCamera();
                        } else {
                            map.autoResize();
                        }
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
