package com.zekibiyikli.nativemindscase.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.zekibiyikli.nativemindscase.enums.NarrationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Ozet metnini cihazin kendi TTS motoruyla seslendirir.
 *
 * Google Books ses dosyasi vermiyor; Sesli modu bu yuzden uretilen Ingilizce
 * ozeti okuyor. Metin cumlelere bolunuyor cunku [TextToSpeech] duraklatmayi
 * desteklemiyor: "duraklat" aslinda durdurmak, "devam et" ise kalinan
 * cumleden yeniden kuyruga vermek demek. Cumle bazli ilerleme ayni zamanda
 * geri/ileri sarmayi da anlamli kiliyor — sure bilinmedigi icin saniye
 * uzerinden sarma yapilamaz.
 */
class BookNarrator(context: Context) {

    private val _state = MutableStateFlow(NarrationState.INITIALIZING)
    val state: StateFlow<NarrationState> = _state.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _sentenceIndex = MutableStateFlow(0)
    val sentenceIndex: StateFlow<Int> = _sentenceIndex.asStateFlow()

    private val _sentenceCount = MutableStateFlow(0)
    val sentenceCount: StateFlow<Int> = _sentenceCount.asStateFlow()

    private var sentences: List<String> = emptyList()
    private var currentText: String? = null

    /**
     * onInit servis baglantisi kurulunca ana is parcaciginda cagriliyor,
     * yani bu atama tamamlandiktan sonra; motor icinden guvenle okunabiliyor.
     */
    private val engine = TextToSpeech(context.applicationContext) { status ->
        _state.value = if (status == TextToSpeech.SUCCESS && applyLanguage()) {
            NarrationState.READY
        } else {
            NarrationState.UNAVAILABLE
        }
    }

    init {
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

            // Kuyruga tum cumleler birden veriliyor; sirayi onStart bildiriyor.
            override fun onStart(utteranceId: String?) {
                utteranceId?.toIntOrNull()?.let { _sentenceIndex.value = it }
            }

            override fun onDone(utteranceId: String?) {
                val finished = utteranceId?.toIntOrNull() ?: return
                if (finished == sentences.lastIndex) _isSpeaking.value = false
            }

            @Deprecated("Eski imza; soyut oldugu icin override edilmek zorunda.")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
            }
        })
    }

    /** Ayni metin tekrar verilirse konum korunur, bastan baslamaz. */
    fun setText(text: String) {
        if (text == currentText) return
        stop()
        currentText = text
        sentences = text.split(SENTENCE_BOUNDARY)
            .map(String::trim)
            .filter(String::isNotEmpty)
        _sentenceCount.value = sentences.size
        _sentenceIndex.value = 0
    }

    fun play() {
        if (_state.value != NarrationState.READY || sentences.isEmpty()) return
        _isSpeaking.value = true
        enqueueFrom(_sentenceIndex.value)
    }

    /** TextToSpeech'te gercek duraklatma yok; kuyruk bosaltilir. */
    fun pause() {
        _isSpeaking.value = false
        engine.stop()
    }

    fun seekTo(index: Int) {
        val target = index.coerceIn(0, sentences.lastIndex.coerceAtLeast(0))
        _sentenceIndex.value = target
        if (_isSpeaking.value) enqueueFrom(target)
    }

    fun release() {
        engine.stop()
        engine.shutdown()
    }

    private fun stop() {
        _isSpeaking.value = false
        engine.stop()
    }

    /**
     * Kalan cumlelerin hepsi tek seferde kuyruga veriliyor; her cumleyi
     * bitiminde tek tek gondermek aralarda duyulur bosluk birakiyordu.
     */
    private fun enqueueFrom(startIndex: Int) {
        sentences.drop(startIndex).forEachIndexed { offset, sentence ->
            engine.speak(
                sentence,
                if (offset == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                null,
                (startIndex + offset).toString()
            )
        }
    }

    private fun applyLanguage(): Boolean {
        // Ozetler Ingilizce uretiliyor; ses paketi yoksa mod kullanilamaz.
        val result = engine.setLanguage(Locale.US)
        return result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    private companion object {
        /** Nokta/soru/unlem sonrasindaki bosluktan boler, isareti cumlede birakir. */
        val SENTENCE_BOUNDARY = Regex("(?<=[.!?])\\s+")
    }
}
