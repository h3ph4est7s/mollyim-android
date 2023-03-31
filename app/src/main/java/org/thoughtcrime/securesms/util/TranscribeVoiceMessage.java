package org.thoughtcrime.securesms.util;

import android.content.Context;

import org.thoughtcrime.securesms.database.model.MediaMmsMessageRecord;
import org.thoughtcrime.securesms.util.task.ProgressDialogAsyncTask;

public class TranscribeVoiceMessage extends ProgressDialogAsyncTask<MediaMmsMessageRecord, Void, String> {
  public TranscribeVoiceMessage(Context context) {
    super(context, "Transcribe", "Transcribing message.");
  }

  @Override protected String doInBackground(MediaMmsMessageRecord... message) {
    message[0].getSlideDeck().getAudioSlide().getUri();
    return "";
  }

  private void prepareAudioData() {

  }
}
