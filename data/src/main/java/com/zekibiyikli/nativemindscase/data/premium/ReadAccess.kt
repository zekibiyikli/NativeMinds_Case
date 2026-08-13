package com.zekibiyikli.nativemindscase.data.premium

/**
 * Bir icerigi acma girisiminin sonucu.
 *
 * Sadece "izin var mi" yetmiyor: izin verilen uc durumun analitikteki
 * karsiligi farkli. Boolean donseydi premium kullanicinin okumasi ya da
 * ayni kitabin tekrar acilmasi da "hak kullanildi" diye raporlanirdi.
 */
enum class ReadAccess {

    /** Premium uye; kotaya hic dokunulmadi. */
    PREMIUM,

    /** Ucretsiz haklardan biri kullanildi. */
    QUOTA_CONSUMED,

    /** Ayni icerik bugun zaten acilmisti; yeni hak harcanmadi. */
    ALREADY_READ,

    /** Gunluk hak bitti. */
    DENIED;

    val isGranted: Boolean get() = this != DENIED
}
