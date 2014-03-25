package com.idiot2ger.beluga.download;

import android.os.Parcel;
import android.os.Parcelable;

import com.idiot2ger.beluga.messagebus.MessageBus;


/**
 * download
 * 
 * @author idiot2ger
 * 
 */
public class Download implements Parcelable {

  public static final int DMSG_START_DOWNLOAD = 1000;
  public static final int DMSG_STOP_DOWNLOAD = DMSG_START_DOWNLOAD + 1;
  public static final int DMSG_RESTART_DOWNLOAD = DMSG_STOP_DOWNLOAD + 1;
  public static final int DMSG_CANCEL_DOWNLOAD = DMSG_RESTART_DOWNLOAD + 1;
  public static final int DMSG_SET_STATUS = DMSG_CANCEL_DOWNLOAD + 1;

  public static final int DEFAULT_PROGRESS_MAX = 100;

  String mDownloadUrl;
  String mSaveFilePath;
  boolean mIsIfExistedWillDelete;
  boolean mIsIfNetworkRightAutoResume;
  int mDownloadId;
  Status mStatus;
  int mProgressMax;


  MessageBus mBus;

  DownloadCallback mCallback;



  /**
   * 
   * @param downloadUrl
   * @param saveFilePath
   */
  private Download(String downloadUrl, String saveFilePath) {
    if (downloadUrl == null || saveFilePath == null) {
      throw new IllegalArgumentException("downloadUrl and saveFilePath must not be null");
    }
    mDownloadId = downloadUrl.hashCode();
    mDownloadUrl = downloadUrl;
    mSaveFilePath = saveFilePath;
    mStatus = Status.STATUS_NONE;
    mProgressMax = DEFAULT_PROGRESS_MAX;

    mBus = MessageBus.getInstance();
  }

  /**
   * start download
   */
  public void startDownload() {
    send(DMSG_START_DOWNLOAD);
  }

  /**
   * restart download
   */
  public void restartDownload() {
    send(DMSG_RESTART_DOWNLOAD);
  }

  /**
   * stop download, you can {@link #restartDownload()} to start the download again, the download
   * will resume(depend on the http get can be resumed or not)
   */
  public void stopDownload() {
    send(DMSG_STOP_DOWNLOAD);
  }

  /**
   * cancel download
   */
  public void cancelDownload() {
    send(DMSG_CANCEL_DOWNLOAD);
  }

  private void send(int msg) {
    mBus.post(msg, Integer.valueOf(mDownloadId));
  }

  public String getDownloadUrl() {
    return mDownloadUrl;
  }

  public String getSaveFilePath() {
    return mSaveFilePath;
  }

  public Status getDownloadStatus() {
    return mStatus;
  }

  void setDownloadStatus(Status status) {
    if (status != mStatus) {
      mStatus = status;
      if (mCallback != null) {
        mCallback.onDownloadStatusChange(mStatus, this);
      }
    }
  }

  void setErrorCode(ErrorCode errorCode) {
    setDownloadStatus(Status.STATUS_STOPED);
    if (mCallback != null) {
      mCallback.onDownloadError(errorCode, this);
    }
  }


  /** Parcelable parts **/

  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(mDownloadId);
    out.writeString(mDownloadUrl);
    out.writeString(mSaveFilePath);
    out.writeInt(mIsIfExistedWillDelete ? 1 : 0);
    out.writeInt(mIsIfNetworkRightAutoResume ? 1 : 0);
    out.writeInt(mStatus.ordinal());
    out.writeInt(mProgressMax);
  }

  public static final Parcelable.Creator<Download> CREATOR = new Parcelable.Creator<Download>() {
    public Download createFromParcel(Parcel in) {
      return new Download(in);
    }

    public Download[] newArray(int size) {
      return new Download[size];
    }
  };


  private Download(Parcel in) {
    mDownloadId = in.readInt();
    mDownloadUrl = in.readString();
    mSaveFilePath = in.readString();
    mIsIfExistedWillDelete = in.readInt() == 1;
    mIsIfNetworkRightAutoResume = in.readInt() == 1;
    mStatus = Status.values()[in.readInt()];
    mProgressMax = in.readInt();

    // need init the bus
    mBus = MessageBus.getInstance();
  }

  /**
   * download status enum
   * 
   * @author idiot2ger
   * 
   */
  public static enum Status {
    /** init status **/
    STATUS_NONE,
    /** already started, but not downloading **/
    STATUS_STARTED,
    /** is downloading **/
    STATUS_DOWNLOADING,
    /** is stoped **/
    STATUS_STOPED,
    /** download finished **/
    STATUS_FINISHED,
    /** canceled by user **/
    STATUS_CANCELED
  }

  /**
   * download error code enum
   * 
   * @author idiot2ger
   * 
   */
  public static enum ErrorCode {
    /** network error **/
    ERROR_NETWORK,
    /** when save file, occur io error **/
    ERROR_FILE_IO,
    /** unknown error **/
    ERROR_UNKNOWN
  }



  /**
   * download callback, <b> all methods run in main thread</b>
   * 
   * @author idiot2ger
   * 
   */
  public static interface DownloadCallback {

    /**
     * download status change callback, <b> method run in main thread</b>
     * 
     * @param status
     * @param download
     */
    public void onDownloadStatusChange(Status status, Download download);

    /**
     * when download occur error callback, <b> method run in main thread</b>
     * 
     * @param errorCode
     * @param download
     */
    public void onDownloadError(ErrorCode errorCode, Download download);

    /**
     * on downloading, <b> method run in main thread</b>
     * 
     * @param progress
     * @param download
     */
    public void onDownloading(int progress, Download download);

  }

  /**
   * download builder
   * 
   * @author idiot2ger
   * 
   */
  public static class DownloadBuilder {

    private Download mDownload;

    private DownloadBuilder(String downloadUrl, String saveFilePath) {
      mDownload = new Download(downloadUrl, saveFilePath);
    }

    public static DownloadBuilder newBuilder(String downloadUrl, String saveFilePath) {
      DownloadBuilder builder = new DownloadBuilder(downloadUrl, saveFilePath);
      return builder;
    }

    public DownloadBuilder setIfExsitedWillDelete(boolean ifExistedWillDelete) {
      mDownload.mIsIfExistedWillDelete = ifExistedWillDelete;
      return this;
    }

    public DownloadBuilder setIfNetworkRightAutoResume(boolean ifNetworkRightAutoResume) {
      mDownload.mIsIfNetworkRightAutoResume = ifNetworkRightAutoResume;
      return this;
    }

    public DownloadBuilder setDownloadCallback(DownloadCallback callback) {
      mDownload.mCallback = callback;
      return this;
    }

    public DownloadBuilder setDownloadMaxProgress(int maxProgress) {
      mDownload.mProgressMax = maxProgress;
      return this;
    }

    public Download build() {
      return mDownload;
    }

  }

}
