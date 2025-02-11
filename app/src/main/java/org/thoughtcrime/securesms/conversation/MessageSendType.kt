package org.thoughtcrime.securesms.conversation

import android.Manifest
import android.content.Context
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.util.CharacterCalculator
import org.thoughtcrime.securesms.util.PushCharacterCalculator
import java.lang.IllegalArgumentException

/**
 * The kinds of messages you can send, e.g. a plain Signal message, an SMS message, etc.
 */
@Parcelize
sealed class MessageSendType(
  @StringRes
  val titleRes: Int,
  @StringRes
  val composeHintRes: Int,
  @DrawableRes
  val buttonDrawableRes: Int,
  @DrawableRes
  val menuDrawableRes: Int,
  @ColorRes
  val backgroundColorRes: Int,
  val transportType: TransportType,
  val characterCalculator: CharacterCalculator,
  open val simName: CharSequence? = null,
  open val simSubscriptionId: Int? = null
) : Parcelable {

  @get:JvmName("usesSmsTransport")
  val usesSmsTransport
    get() = false

  @get:JvmName("usesSignalTransport")
  val usesSignalTransport
    get() = transportType == TransportType.SIGNAL

  fun calculateCharacters(body: String): CharacterCalculator.CharacterState {
    return characterCalculator.calculateCharacters(body)
  }

  fun getSimSubscriptionIdOr(fallback: Int): Int {
    return simSubscriptionId ?: fallback
  }

  open fun getTitle(context: Context): String {
    return context.getString(titleRes)
  }

  /**
   * A type representing a basic Signal message.
   */
  @Parcelize
  object SignalMessageSendType : MessageSendType(
    titleRes = R.string.ConversationActivity_transport_signal,
    composeHintRes = R.string.ConversationSettingsFragment__message,
    buttonDrawableRes = R.drawable.ic_send_lock_24,
    menuDrawableRes = R.drawable.ic_secure_24,
    backgroundColorRes = R.color.core_ultramarine,
    transportType = TransportType.SIGNAL,
    characterCalculator = PushCharacterCalculator()
  )

  enum class TransportType {
    SIGNAL
  }

  companion object {

    private val TAG = Log.tag(MessageSendType::class.java)

    /**
     * Returns a list of all available [MessageSendType]s. Requires [Manifest.permission.READ_PHONE_STATE] in order to get available
     * SMS options.
     */
    @JvmStatic
    fun getAllAvailable(context: Context): List<MessageSendType> {
      return listOf(SignalMessageSendType)
    }

    @JvmStatic
    fun getFirstForTransport(context: Context, transportType: TransportType): MessageSendType {
      return getAllAvailable(context).firstOrNull { it.transportType == transportType } ?: throw IllegalArgumentException("No options available for desired type $transportType!")
    }
  }
}
