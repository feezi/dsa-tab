/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.map;

import java.io.File;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;

/**
 * @author Ganymede
 * 
 */
public class MapTileAbsoluteFilesystemProvider extends MapTileFileStorageProviderBase {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory.getLogger(MapTileFilesystemProvider.class);

	// ===========================================================
	// Fields
	// ===========================================================

	private final long mMaximumCachedFileAge;

	private ITileSource mTileSource;

	private String basePath;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileAbsoluteFilesystemProvider(String basePath, final IRegisterReceiver pRegisterReceiver) {
		this(basePath, pRegisterReceiver, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	public MapTileAbsoluteFilesystemProvider(String basePath, final IRegisterReceiver pRegisterReceiver,
			final ITileSource aTileSource) {
		this(pRegisterReceiver, aTileSource, DEFAULT_MAXIMUM_CACHED_FILE_AGE);
		this.basePath = basePath;
	}

	/**
	 * Provides a file system based cache tile provider. Other providers can
	 * register and store data in the cache.
	 * 
	 * @param pRegisterReceiver
	 */
	public MapTileAbsoluteFilesystemProvider(final IRegisterReceiver pRegisterReceiver, final ITileSource pTileSource,
			final long pMaximumCachedFileAge) {
		super(pRegisterReceiver, NUMBER_OF_TILE_FILESYSTEM_THREADS, TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);
		mTileSource = pTileSource;

		mMaximumCachedFileAge = pMaximumCachedFileAge;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean getUsesDataConnection() {
		return false;
	}

	@Override
	protected String getName() {
		return "File System Cache Provider";
	}

	@Override
	protected String getThreadGroupName() {
		return "filesystem";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	};

	@Override
	public int getMinimumZoomLevel() {
		return mTileSource != null ? mTileSource.getMinimumZoomLevel() : MAXIMUM_ZOOMLEVEL;
	}

	@Override
	public int getMaximumZoomLevel() {
		return mTileSource != null ? mTileSource.getMaximumZoomLevel() : MINIMUM_ZOOMLEVEL;
	}

	@Override
	public void setTileSource(final ITileSource pTileSource) {
		mTileSource = pTileSource;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class TileLoader extends MapTileModuleProviderBase.TileLoader {

		@Override
		public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException {

			if (mTileSource == null) {
				return null;
			}

			final MapTile tile = pState.getMapTile();

			// if there's no sdcard then don't do anything
			if (!getSdCardAvailable()) {
				if (DEBUGMODE) {
					logger.debug("No sdcard - do nothing for tile: " + tile);
				}
				return null;
			}

			// Check the tile source to see if its file is available and if so,
			// then render the
			// drawable and return the tile
			final File file = new File(basePath, mTileSource.getTileRelativeFilenameString(tile));
			if (file.exists()) {

				// Check to see if file has expired
				final long now = System.currentTimeMillis();
				final long lastModified = file.lastModified();
				final boolean fileExpired = lastModified < now - mMaximumCachedFileAge;

				if (!fileExpired) {
					// If the file has not expired, then render it and return
					// it!
					try {
						Drawable drawable;
						drawable = mTileSource.getDrawable(file.getPath());

						return drawable;
					} catch (LowMemoryException e) {
						// low memory so empty the queue
						logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
						throw new CantContinueException(e);
					}
				} else {
					// If the file has expired then we render it, but we return
					// it as a candidate
					// and then fail on the request. This allows the tile to be
					// loaded, but also
					// allows other tile providers to do a better job.
					try {
						final Drawable drawable = mTileSource.getDrawable(file.getPath());
						tileLoaded(pState, drawable);
						return null;
					} catch (LowMemoryException e) {
						// low memory so empty the queue
						logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
						throw new CantContinueException(e);
					}
				}
			}

			// If we get here then there is no file in the file cache
			return null;
		}
	}
}
