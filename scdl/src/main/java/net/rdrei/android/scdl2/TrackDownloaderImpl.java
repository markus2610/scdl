package net.rdrei.android.scdl2;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import net.rdrei.android.scdl2.ApplicationPreferences.StorageType;
import net.rdrei.android.scdl2.api.entity.TrackEntity;

import java.io.File;
import java.io.IOException;

import roboguice.util.Ln;
import roboguice.util.SafeAsyncTask;

/**
 * Class for handling the downloading of tracks.
 *
 * @author pascal
 */
public class TrackDownloaderImpl implements TrackDownloader {

	@Inject
	private Context mContext;

	@Inject
	private DownloadManager mDownloadManager;

	@Inject
	private ApplicationPreferences mPreferences;

	@Inject
	private Tracker mTracker;

	private final Uri mUri;
	private final TrackEntity mTrack;
	private final Handler mHandler;

	@Inject
	public TrackDownloaderImpl(@Assisted final Uri mUri, @Assisted final TrackEntity mTrack,
			@Assisted final Handler handler) {
		super();
		this.mUri = mUri;
		this.mTrack = mTrack;
		mHandler = handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rdrei.android.scdl2.TrackDownloader#enqueue()
	 */
	@Override
	public void enqueue() throws IOException {
		final StartDownloadTask startDownloadTask = new StartDownloadTask(mHandler);
		startDownloadTask.execute();
	}

	/**
	 * Check if the given path is writable and attempts to create it.
	 */
	public static boolean checkAndCreateTypePath(final File path) {
		if (!path.exists()) {
			Ln.i("Path %s doesn't exist, creating...", path.toString());
			if (!path.mkdirs()) {
				Ln.w("Creating directory failed!");
				return false;
			}
		}

		if (BuildConfig.DEBUG) {
			Ln.d("checkAndCreateTypePath isDirectory:" + path.isDirectory());
			Ln.d("checkAndCreateTypePath canWrite:" + path.canWrite());
		}
		return path.isDirectory() && path.canWrite();
	}

	/**
	 * Creates a new download manager request based on the given uri.
	 *
	 * @param uri
	 * @return
	 * @throws IOException If directory can't be used for saving the file.
	 */
	@TargetApi(11)
	private DownloadManager.Request createDownloadRequest(final Uri uri) throws IOException {
		final Request request = new Request(uri);

		setRequestStorage(request);
		request.setTitle(mTrack.getTitle());
		request.setDescription(mContext.getString(R.string.download_description));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// We have an audio file, please scan it!
			request.allowScanningByMediaScanner();
		}

		return request;
	}

	private void setRequestStorage(final Request request) throws IOException {
		final StorageType type = mPreferences.getStorageType();
		final File typePath = mPreferences.getStorageDirectory();
		String filename = mTrack.getDownloadFilename();

		if (type == StorageType.LOCAL) {
			filename += Config.TMP_DOWNLOAD_POSTFIX;
		}

		// The preferences panel already tries to create the path, but it could
		// have been removed in the meantime, so we rather double-check.
		if (!checkAndCreateTypePath(typePath)) {
			throw new IOException(
					String.format("Can't open directory %s to write.", typePath.toString()));
		}

		final Uri destinationUri = Uri.withAppendedPath(Uri.fromFile(typePath), filename);
		Ln.d("Local destination URI: %s", destinationUri.toString());
		request.setDestinationUri(destinationUri);
	}

	private class StartDownloadTask extends SafeAsyncTask<Void> {

		public StartDownloadTask(final Handler handler) {
			super(handler);
		}

		@Override
		public Void call() throws Exception {
			Ln.d("Starting download of %s.", mUri.toString());
			final Request request;
			request = createDownloadRequest(mUri);

			mDownloadManager.enqueue(request);
			return null;
		}

		/**
		 * Catches exceptions during the creation of the download request and bubbles them up using
		 * the provided handler.
		 */
		@Override
		protected void onException(final Exception e) throws RuntimeException {
			super.onException(e);
			final Message msg;

			Ln.i("Track download exception encountered.", e);
			if (handler == null) {
				trackDownloadError("DOWNLOAD_HANDLER_ERROR");
				return;
			}

			if (e instanceof IOException) {
				msg = handler.obtainMessage(MSG_DOWNLOAD_STORAGE_ERROR);
				trackDownloadError("DOWNLOAD_STORAGE_ERROR");
			} else {
				msg = handler.obtainMessage(MSG_DOWNLOAD_ERROR);
				trackDownloadError("DOWNLOAD_REQUEST_ERROR");
			}

			handler.sendMessage(msg);
		}

		private void trackDownloadError(final String description) {
			mTracker.send(
					new HitBuilders.ExceptionBuilder()
							.setDescription(description)
							.setFatal(false)
							.build()
			);
		}
	}
}
