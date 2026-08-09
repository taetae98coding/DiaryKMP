package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Stable
public class DiaryMapState(
    initialProvider: DiaryMapProvider,
    initialCoordinate: DiaryMapCoordinate? = null,
    initialSpot: DiaryMapCoordinate? = null,
) {
    public var provider: DiaryMapProvider by mutableStateOf(initialProvider)
        private set

    public var spot: DiaryMapCoordinate? by mutableStateOf(initialSpot)
        private set

    public var pins: List<DiaryMapPin> by mutableStateOf(emptyList())
        private set

    public var bounds: DiaryMapBounds? by mutableStateOf(null)
        private set

    internal var camera: DiaryMapCamera? =
        initialCoordinate?.let { coordinate ->
            DiaryMapCamera(
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
            )
        }
        private set

    private val moveCommandChannel = Channel<DiaryMapCamera>(Channel.CONFLATED)
    internal val moveCommand: Flow<DiaryMapCamera> = moveCommandChannel.receiveAsFlow()

    public val coordinate: DiaryMapCoordinate?
        get() =
            camera?.let { camera ->
                DiaryMapCoordinate(latitude = camera.latitude, longitude = camera.longitude)
            }

    public fun select(provider: DiaryMapProvider) {
        this.provider = provider
    }

    public fun selectSpot(coordinate: DiaryMapCoordinate?) {
        spot = coordinate
    }

    public fun updatePins(pins: List<DiaryMapPin>) {
        this.pins = pins
    }

    public fun moveTo(coordinate: DiaryMapCoordinate) {
        val moved =
            DiaryMapCamera(
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                zoom = camera?.zoom ?: DiaryMapCamera.NEIGHBORHOOD_ZOOM,
            )

        camera = moved
        moveCommandChannel.trySend(moved)
    }

    internal fun moveCamera(camera: DiaryMapCamera) {
        this.camera = camera
        bounds = camera.bounds
    }

    public companion object {
        internal val Saver: Saver<DiaryMapState, Any> =
            mapSaver(
                save = { state ->
                    mapOf(
                        PROVIDER_KEY to state.provider.name,
                        LATITUDE_KEY to state.camera?.latitude,
                        LONGITUDE_KEY to state.camera?.longitude,
                        ZOOM_KEY to state.camera?.zoom,
                        BOUNDS_SOUTH_KEY to state.bounds?.south,
                        BOUNDS_NORTH_KEY to state.bounds?.north,
                        BOUNDS_WEST_KEY to state.bounds?.west,
                        BOUNDS_EAST_KEY to state.bounds?.east,
                        SPOT_LATITUDE_KEY to state.spot?.latitude,
                        SPOT_LONGITUDE_KEY to state.spot?.longitude,
                    )
                },
                restore = { saved ->
                    val spotLatitude = saved[SPOT_LATITUDE_KEY] as Double?
                    val spotLongitude = saved[SPOT_LONGITUDE_KEY] as Double?

                    DiaryMapState(
                        initialProvider = DiaryMapProvider.valueOf(saved[PROVIDER_KEY] as String),
                        initialSpot =
                            if (spotLatitude == null || spotLongitude == null) {
                                null
                            } else {
                                DiaryMapCoordinate(latitude = spotLatitude, longitude = spotLongitude)
                            },
                    ).apply {
                        val latitude = saved[LATITUDE_KEY] as Double?
                        val longitude = saved[LONGITUDE_KEY] as Double?
                        val zoom = saved[ZOOM_KEY] as Double?

                        if (latitude != null && longitude != null && zoom != null) {
                            moveCamera(
                                DiaryMapCamera(
                                    latitude = latitude,
                                    longitude = longitude,
                                    zoom = zoom,
                                    bounds = saved.restoreBoundsOrNull(),
                                ),
                            )
                        }
                    }
                },
            )

        private const val PROVIDER_KEY = "provider"
        private const val LATITUDE_KEY = "latitude"
        private const val LONGITUDE_KEY = "longitude"
        private const val ZOOM_KEY = "zoom"
        private const val BOUNDS_SOUTH_KEY = "boundsSouth"
        private const val BOUNDS_NORTH_KEY = "boundsNorth"
        private const val BOUNDS_WEST_KEY = "boundsWest"
        private const val BOUNDS_EAST_KEY = "boundsEast"
        private const val SPOT_LATITUDE_KEY = "spotLatitude"
        private const val SPOT_LONGITUDE_KEY = "spotLongitude"

        private fun Map<String, Any?>.restoreBoundsOrNull(): DiaryMapBounds? {
            val south = get(BOUNDS_SOUTH_KEY) as Double?
            val north = get(BOUNDS_NORTH_KEY) as Double?
            val west = get(BOUNDS_WEST_KEY) as Double?
            val east = get(BOUNDS_EAST_KEY) as Double?

            if (listOf(south, north, west, east).any { value -> value == null }) return null

            return DiaryMapBounds(
                south = checkNotNull(south),
                north = checkNotNull(north),
                west = checkNotNull(west),
                east = checkNotNull(east),
            )
        }
    }
}

@Composable
public fun rememberDiaryMapState(
    initialProvider: DiaryMapProvider = DiaryMapProvider.NAVER,
    initialCoordinate: DiaryMapCoordinate? = null,
): DiaryMapState =
    rememberSaveable(saver = DiaryMapState.Saver) {
        DiaryMapState(
            initialProvider = initialProvider,
            initialCoordinate = initialCoordinate,
        )
    }
