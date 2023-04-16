package org.thoughtcrime.securesms.osm

import android.content.Context
import org.osmdroid.tileprovider.IMapTileProviderCallback
import org.osmdroid.tileprovider.IRegisterReceiver
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.modules.IFilesystemCache
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.MapTileAreaBorderComputer
import org.osmdroid.util.MapTileAreaZoomComputer

class MapTileProvider(
  context: Context,
  tileSource: ITileSource = TileSourceFactory.DEFAULT_TILE_SOURCE,
  registerReceiver: IRegisterReceiver = SimpleRegisterReceiver(context),
) : MapTileProviderArray(tileSource, registerReceiver), IMapTileProviderCallback {

  companion object {
    @JvmStatic
    fun create(context: Context) = MapTileProvider(context)
  }

  private val cacheWriter = SingleSessionDiskTileWriter(context)

  private val networkAvailabilityCheck = NetworkAvailabliltyCheck(context)

  private val cacheProvider = MapTileFilesystemProvider(tileSource, cacheWriter)

  private val downloaderProvider = MapTileDownloader(tileSource, cacheWriter, networkAvailabilityCheck)

  init {
    mTileProviderList.add(cacheProvider)
    mTileProviderList.add(downloaderProvider)

    // protected-cache-tile computers

    // protected-cache-tile computers
    tileCache.protectedTileComputers.add(MapTileAreaZoomComputer(-1))
    tileCache.protectedTileComputers.add(MapTileAreaBorderComputer(1))
    tileCache.setAutoEnsureCapacity(false)
    tileCache.setStressedMemory(false)

    // pre-cache providers

    // pre-cache providers
    tileCache.preCache.addProvider(cacheProvider)
    tileCache.preCache.addProvider(downloaderProvider)

    // tiles currently being processed
    tileCache.protectedTileContainers.add(this)
  }

  override fun getTileWriter(): IFilesystemCache {
    return cacheWriter
  }
}
