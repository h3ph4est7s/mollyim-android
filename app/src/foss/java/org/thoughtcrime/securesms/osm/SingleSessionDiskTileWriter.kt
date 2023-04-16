package org.thoughtcrime.securesms.osm

import android.content.Context
import android.graphics.drawable.Drawable
import org.osmdroid.tileprovider.modules.IFilesystemCache
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.util.EncryptedStreamUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.util.UUID

private val TAG = Log.tag(SingleSessionDiskTileWriter::class.java)

private val SingleSessionId = SecureRandom().nextInt()

class SingleSessionDiskTileWriter(private val context: Context) : IFilesystemCache {

  private val tileCacheDir = File(context.cacheDir, "tiles")

  init {
    tileCacheDir.mkdir()
  }

  private fun buildTileFile(tileSource: ITileSource, pMapTileIndex: Long): File {
    val pathName = tileSource.getTileRelativeFilenameString(pMapTileIndex) + SingleSessionId.toString(36)
    val uuid = UUID.nameUUIDFromBytes(pathName.toByteArray())
    return File(tileCacheDir, uuid.toString())
  }

  override fun saveFile(tileSource: ITileSource, pMapTileIndex: Long, pStream: InputStream, pExpirationTime: Long): Boolean {
    val tileFile = buildTileFile(tileSource, pMapTileIndex)
    return try {
      EncryptedStreamUtils.getOutputStream(context, tileFile).use { output ->
        pStream.copyTo(output)
      }
      tileFile.deleteOnExit()
      true
    } catch (e: IOException) {
      Log.w(TAG, "Failed to save tile $pMapTileIndex to cache")
      false
    }
  }

  override fun exists(tileSource: ITileSource, pMapTileIndex: Long): Boolean =
    buildTileFile(tileSource, pMapTileIndex).exists()

  override fun onDetach() = Unit

  override fun remove(tileSource: ITileSource, pMapTileIndex: Long): Boolean =
    buildTileFile(tileSource, pMapTileIndex).delete()

  override fun getExpirationTimestamp(pTileSource: ITileSource, pMapTileIndex: Long): Long? = null

  override fun loadTile(tileSource: ITileSource, pMapTileIndex: Long): Drawable? {
    val tileFile = buildTileFile(tileSource, pMapTileIndex)
    if (!tileFile.exists()) {
      return null
    }
    EncryptedStreamUtils.getInputStream(context, tileFile).use { input ->
      return tileSource.getDrawable(input)
    }
  }
}
