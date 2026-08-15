# NativeMinds Case — Kitap Keşif Uygulaması

Kitap keşfetme, detayını okuma ve sesli dinleme uygulaması. Kullanıcı kategori bazlı bir
feed'de gezinir, arama yapar, favorilerine ekler; detay ekranında kitabın yapay zekâ ile
üretilmiş özetini okur ya da cihazın TTS motoruyla dinler. Ücretsiz kullanıcı günde 3 içerik
açabilir, sonrasında premium duvarına çıkar.

Mimari ve akış şemasını (`contents/Diagram.pdf`) işe başlarken çizdim, uygulama
ilerledikçe koda göre güncelledim: katmanlar ve modüller, feed → premium mi? → günlük limit
aşıldı mı? → içerik ya da premium sayfası kapısı, cross-cutting olarak Hilt + observability.
Şemadaki her kutunun depoda bir karşılığı var.

## Ekranlar ve akışlar

| Ekran | İçerik |
|---|---|
| Splash | Lottie animasyonu, sabit süre sonra Home'a geçer (geri yığınından düşer) |
| Home | Kategori seçimli, sonsuz kaydırmalı içerik feed'i (Paging 3 + RemoteMediator) |
| Search | Debounce'lu arama önerileri + öne çıkan kategori şeridi |
| Search Results | Kelime ve/veya kategori filtresiyle sonuç ızgarası |
| Detail | Kapak, künye, **Kitap / Sesli** modu, favoriye ekleme, AI özeti, kota kapısı |
| Favorites | Room'dan okunan, çevrimdışı da çalışan favori listesi |
| Premium | Haftalık / aylık / yıllık plan seçimi, satın alma (şimdilik stub) |

## Teknolojiler

**Dil / UI:** Kotlin, Jetpack Compose, Material 3, Navigation Compose (type-safe `@Serializable` route'lar), Lottie, Coil 3

**Mimari / DI:** Çok modüllü (`:app`, `:core`, `:data`), MVVM + Repository, Hilt, Coroutines & Flow

**Veri:** Retrofit + kotlinx.serialization, OkHttp (interceptor'lar), Room (feed cache, favoriler, özet cache), DataStore Preferences, Paging 3 + `RemoteMediator`, WorkManager

**Servisler:** Google Books API (içerik), Anthropic Claude API (özet üretimi), Android TextToSpeech (sesli mod), Firebase Analytics + Crashlytics, Play Billing (bağımlılık hazır, akış stub)

**Test:** JUnit4, MockWebServer, coroutines-test, Room/Work testing — `:data` altında 30 birim testi

### Modül sınırları

```
:app    Compose ekranları, ViewModel'ler, navigation, TTS
  ↓
:data   Repository'ler, Retrofit servisleri, Room, DataStore, WorkManager
  ↓
:core   Outcome/AppException, AppConfig, TimeProvider, Analytics/Crash sarmalayıcıları
```

`:app` hiçbir yerde Retrofit/Room tipine dokunmuyor; ağ istisnaları repository katmanında
`AppException`'a çevriliyor, ekranlar sadece `Outcome<T>` (Loading/Success/Failure) görüyor.

## Kurulum

**1. `app/google-services.json` (zorunlu).** Firebase yapılandırması her geliştirici/ortam için
farklı olduğu ve gizli bilgi taşıdığı için repoya girmiyor. Bu dosya olmadan Google Services
plugin'i build'i durdurur. Firebase Console'da `com.zekibiyikli.nativemindscase` applicationId'si
ile bir Android uygulaması oluşturup indirilen dosyayı `app/` altına koymak yeterli.

**2. `local.properties` (ikisi de opsiyonel).** Repoya girmez; değerler `BuildConfig` alanlarına
gömülür.

```properties
GOOGLE_BOOKS_API_KEY=...   # boş bırakılabilir — anahtarsız istekler de çalışır, kota düşüktür
ANTHROPIC_API_KEY=...      # boş bırakılırsa AI özeti kapanır, Google Books açıklamasına düşülür
```

**3. Derleme ve testler.**

```bash
./gradlew :app:assembleDebug
./gradlew :data:testDebugUnitTest
```

Anahtarsız klonlayan biri de uygulamayı çalıştırıp gezebilir; özet üretimi kapanır, gerisi
çalışır. API'yi keşfederken kullandığım istekler
`contents/Google_Books_API.postman_collection.json` içinde.

# Süreç

## 1. Anahtar kararlar ve gerekçeleri

**Kitap kaynağı: OpenLibrary değil, Google Books.** Önce OpenLibrary'ye baktım — künye
veriyor ama kitap açıklaması dönmüyor. Bu uygulamanın detay ekranı da premium değeri de
metnin üstüne kurulu olduğu için elemesi kolay oldu. Google Books `description` alanını
dönüyor, üstüne kapak/yazar/sayfa sayısı/kategori gibi zengin metadata veriyor ve anahtarsız
da çalışıyor; repoyu anahtarsız klonlayan biri uygulamayı gezebiliyor.

**Anasayfa neden kategori bazlı.** Google Books'ta "kitapları listele" diye bir endpoint yok,
her şey sorgu üzerinden dönüyor — yani "popüler kitaplar" gibi hazır bir feed alamıyorsun.
Bu yüzden anasayfayı kategori seçimli kurdum: seçilen kategori `subject:` filtresine gidiyor
ve sayfalama oradan yürüyor. Kategori listesi de sabit (`Subjects.kt`), çünkü "kategorileri
listele" endpoint'i de yok. API'nin kısıtını gizlemek yerine ürün kararına çevirdim.

**Özet üretimi için Claude.** Fark eden kısım model değil, sözleşme: Anthropic'in
`output_config` + JSON schema desteğiyle modelden `{"known": bool, "summary": string}`
istiyorum. `known` alanı özellikle var — modelin tanımadığı kitapta konu uydurmasındansa
"bilmiyorum" demesini istiyorum; `known=false` gelince özet hiç gösterilmiyor, Google
Books'un kendi açıklamasına düşülüyor. Uydurma özet, eksik özetten çok daha kötü.

**Domain (UseCase) katmanı yok.** İlk şemada vardı; koymadım. Bu uygulamada use case'lerin
tamamı tek repository çağrısına delege edecekti (`GetFeedUseCase → repository.pagedFeed()`).
Katman başına düşen iş yokken sadece dosya sayısını artırır. Kota kuralı ise
`PremiumRepository.registerRead()` içinde, atomik olması gerektiği için zaten doğru yerde.
Tek gerçek aday `DetailViewModel`'deki özet orkestrasyonuydu (kitap + premium durumu
birleşiyor, ona göre Claude'a gidiliyor ya da gidilmiyor); tek kullanıcısı olduğu için onu da
bilerek yerinde bıraktım. Ürün büyüseydi ilk ekleyeceğim katman bu olurdu — modül sınırları
zaten hazır.

**Tek doğruluk kaynağı Room.** Feed ve favoriler her zaman yerelden okunuyor; ağ sadece
cache'i tazeliyor (`FeedRemoteMediator`). Sonucu: uçak modunda uygulama açılıyor, son görülen
feed ve favoriler listeleniyor. Cache 1 saatten tazeyse açılışta ağa hiç gidilmiyor
(`SKIP_INITIAL_REFRESH`), kullanıcı aşağı çekerek her an yenileyebiliyor.

**Kota, "bugün okunan ID kümesi" olarak tutuluyor.** Sayaç yerine küme, çünkü aynı kitabı
tekrar açmak haktan düşmemeli. Kontrol ve düşüm tek `DataStore.edit` bloğunda, yani atomik;
hızlı çift tık iki hak yemiyor.

**`ReadAccess` boolean değil enum.** PREMIUM / QUOTA_CONSUMED / ALREADY_READ / DENIED. Boolean
dönseydi analitikte "premium üyenin okuması" ile "ücretsiz hak harcanması" aynı event'e
düşerdi ve funnel ölçülemezdi.

**AI özeti sadece premium'da üretiliyor.** Ücretsiz kullanıcıda Claude'a hiç istek atılmıyor.
Bu hem gerçek bir maliyet kararı hem de premium'a somut bir değer veriyor — case'te premium'un
"neyi açtığı" tanımsızdı.

**Özetler Room'da cache'leniyor, `null` sonuçlar dâhil.** Model bir kitabı tanımadıysa bunu da
yazıyorum; yoksa o kitap her açıldığında aynı ücretli istek tekrar giderdi.

**Sesli mod = üretilen özetin TTS'i.** Google Books ses dosyası vermiyor. Cümle bazlı ilerleme
seçtim çünkü `TextToSpeech`'te gerçek pause yok ("duraklat" = stop, "devam" = kalınan cümleden
yeniden kuyruğa verme) ve toplam süre bilinmediği için saniye bazlı sarma yapılamıyor.

**Play Billing bağlandı ama satın alma stub.** Sunucu tarafı doğrulama olmadan gerçek bir
satın alma akışı yazmak, çalışıyormuş gibi görünen ama güvenilmez bir kod demek. Bağımlılık ve
plan modeli hazır, `PremiumViewModel.onPurchaseClick()` hakkı doğrudan veriyor ve TODO ile
işaretli. Case kapsamında bunu kapatmak yerine açıkça eksik bırakmayı tercih ettim.

**`AppConfig` tek yerde.** Süreler, limitler, sayfa boyutu, timeout'lar — davranışı belirleyen
her değer. Protokol sabitleri ve tekil ekran padding'leri bilerek dışarıda: her sayıyı tek
dosyaya toplamak okunurluğu düşürüyor.

**`langRestrict=en`.** Google Books açıklamayı çevirmiyor; açıklama hangi baskıdaysa o dilde
geliyor. Tutarlı İngilizce içerik için sonuçların kendisini İngilizce baskılarla sınırlamak tek
yol. Bunu API'yi Postman'da kurcalarken gördüm, kodu yazarken değil.

## 2. AI'ı nasıl yönlendirdim

**Önce sözleşme, sonra kod.** Doğrudan "şu ekranı yaz" demedim. Sırayla: modül sınırları →
`Outcome`/`AppException` → repository arayüzleri → Room şeması → ekranlar. Arayüzler
sabitlendikten sonra üretilen kodun yanlış katmana sızması pratikte bitti; ilk denemelerde
Retrofit tipi `:app`'e kadar geliyordu.

**Kısıtları prompt'a koydum, sonuca bakıp düzeltmedim.** "Google Books `maxResults` için 40
üstünü kabul etmiyor", "`:app` Room'u görmesin", "yorumlar ne yaptığını değil neden öyle
yaptığını anlatsın" gibi kurallar baştan verilince ikinci turlarda tekrar etmek gerekmedi.

**Sürümleri modele sordurmadım.** Model kütüphane sürümlerini uyduruyor ya da eski biliyor.
Version catalog'daki her sürüm Maven metadata'sından `curl` ile doğrulandı; bu komutları
Claude Code'un izin listesine ekledim ki her seferinde onay sormadan kontrol edebilsin.

**Hata mesajını ham veriyorum.** Gradle/KSP çıktısını yorumlamadan yapıştırmak, benim
"şurada hata var" özetimden belirgin şekilde daha isabetli sonuç veriyor — özellikle Hilt ve
KSP hatalarında.

**İlk cevap tutmadığında yeniden çerçeveledim.** Örnek: özet üretiminde ilk yaklaşım "modelden
düz metin iste, gelen metni göster"di ve model tanımadığı kitaplar için akıcı ama tamamen
uydurma özetler yazıyordu. "Prompt'a 'uydurma' ekle" yerine problemi değiştirdim: JSON schema
+ `known` bayrağı, yani modele bilmeme seçeneği veren bir sözleşme. Sorun bir daha çıkmadı.

**Bir dosyayı bitirmeden diğerine geçmedim.** Uzun oturumlarda "hepsini birden yaz" çıktısı
derleniyor ama detayları savruk oluyor; ekran ekran gidip her adımda derleyip çalıştırmak daha
hızlı çıktı.

## 3. AI çıktısını gözden geçirip düzelttiğim yerler

**Tasarımı AI'a çizdirmekten vazgeçtim.** Ekranları önce Google Stitch ve Claude'un tasarım
üretimiyle çıkarmayı denedim. Gelenler tek tek fena değildi ama ürünle ilişkisi kurulmamış,
birbirine benzeyen şablonlardı: hangi bilginin neden orada durduğunu taşımıyorlardı. Bir tur
daha prompt denemek yerine yaklaşımı değiştirdim — ekran taslaklarını (yerleşim, hiyerarşi,
hangi bilgi nerede) kendim çıkardım, sonra o taslağı adım adım Claude ile koda geçirdim: bir
ekran, bir bileşen, her adımda derleyip bakarak. Tasarım kararı bende kaldı, üretim hızı
AI'da. Detay ekranındaki Kitap/Sesli ayrımı ve premium sayfasının plan sıralaması bu taslaktan
geliyor, üretilen tasarımlardan değil.

**Paging'in varsayılan `initialLoadSize`'ı.** Üretilen `PagingConfig` varsayılanı bırakıyordu;
Paging ilk yüklemede sayfa boyutunun 3 katını ister, yani 60 kayıt. Google Books 40 üstünü
reddediyor — ilk açılışta feed boş gelecekti. `initialLoadSize` sayfa boyutuna eşitlendi,
`maxResults` ayrıca `coerceAtMost(40)` ile sınırlandı.

**RemoteMediator'da sayfa ilerletme.** Üretilen kod bir sonraki `startIndex`'i *filtrelenmiş*
liste boyutuyla ilerletiyordu. Başlıksız/kapaksız kayıtları eledikten sonra bu, API tarafında
kayıt atlanmasına ya da tekrar etmesine yol açar. Ham `received.size` ile ilerletilmesi
gerekiyordu; sessizce yanlış davranan, testte de kolay yakalanmayan bir hataydı.

**`CancellationException` yutuluyordu.** Mediator'daki genel `catch (Throwable)` iptali de
`MediatorResult.Error`'a çeviriyordu; bu Paging'in yeniden denemesini bozuyor. Ayrı yakalanıp
yeniden fırlatılıyor. Aynı hatanın bir varyantı `refreshFavorites` ve worker'da da vardı.

**OkHttp sürüm çakışması.** Retrofit 3.0.0 ve Coil 3 aynı OkHttp'yi çekiyor; catalog'da
ayrışınca runtime'da patlıyordu. Model "sürümü yükselt" dedi, ben tek sürüme pinledim ve
nedenini catalog'a yorum olarak yazdım — bir sonraki kişi ayırmasın diye.

**Kota kontrolü başta atomik değildi.** İlk hâli "oku → karşılaştır → yaz" şeklinde ayrı
adımlardaydı. Tek `edit` bloğuna aldım; ayrıca `TimeProvider` soyutlaması ekledim ki gün
değişimi testte gerçekten sınanabilsin (`FakeTimeProvider.advanceDays`).

**API anahtarı log'a sızıyordu.** OkHttp logging interceptor URL'i olduğu gibi basıyor, yani
`?key=...`. Debug'da olsa da kabul edilebilir değil; `key=***` maskeleyen bir logger yazıldı.

**Kota kontrolü bitmeden içerik gösteriliyordu.** İlk hâlde detay ekranı açılır açılmaz
render ediliyor, kota sonucu sonra geliyordu — yani duvarın arkasındaki içerik bir kare de
olsa görünüyordu. `AccessState.CHECKING` eklendi; kontrol bitmeden içerik çizilmiyor.

**Bir gözden geçirme turunu bilerek ayrı commit yaptım** (`CodeReview`): kullanılmayan tema/
tipografi tanımları, ViewModel'de birikmiş ölü state ve tekrar eden UI parçaları temizlendi;
Home'un state'i `HomeUiState`'e taşındı. Üretilen kod çalışıyordu ama artık okunmuyordu.

## 4. Hızlanmak için kurduklarım

- **Claude Code + izin listesi** (`.claude/settings.local.json`): `assembleDebug`,
  `testDebugUnitTest`, emülatör başlatma ve Maven metadata `curl`'leri allowlist'te. Amaç
  "yaz → derle → testleri koştur" döngüsünün her turda onay beklemeden dönmesi.
- **`claude-api` skill'i**: Anthropic tarafının model ID'leri, `output_config`/JSON schema ve
  token parametreleri için; modelin ezberden verdiği API şeklini kullanmak yerine.
- **Postman koleksiyonu** (`contents/Google_Books_API.postman_collection.json`): kod yazmadan önce
  Google Books'un gerçek yanıtlarını gezdim. `langRestrict` kararı ve `maxResults=40` sınırı
  buradan çıktı — API'yi kodda değil, API'de öğrenmek daha ucuz.
- **Yorumlar için tek kural**: "ne" değil "neden" yazılacak. Bu kural sayesinde kod tabanı
  aynı zamanda bu README'nin kaynağı oldu; kararların gerekçeleri ilgili dosyada duruyor.
- **Denenip bırakılan**: tasarım üretimi için Google Stitch ve Claude design. Hız kazandırmadı,
  taslağı kendim çıkarmak daha hızlı oldu (bkz. 3. bölüm).

**MCP kurmadım, ama nerede kuracağımı biliyorum.** Bu projede iki dış ihtiyacım vardı — Maven
metadata'sından sürüm doğrulama ve Google Books yanıtlarını gezme — ikisi de tek seferlikti ve
`curl` + Postman ile hallolduğu için araya bir server koymanın getirisi yoktu. Gerçek
kullanıcıya çıksaydı ilk bağlayacağım Firebase MCP server'ı olurdu: Crashlytics issue'larını ve
stack trace'lerini doğrudan okuyup düzeltme döngüsünü kısaltmak için (`crashlytics_get_issue`,
`crashlytics_list_events`). Şu an bağlamak anlamsız — kullanıcı yok, crash yok, okuyacağı liste
boş. Araç, tekrar eden ve yapısal erişim gerektiren işte kazandırır; tek seferlik işte maliyeti
getirisinden fazla.

## 5. Bilinerek eksik bırakılanlar

- **Play Billing satın alma akışı** — ürün sorgusu, purchase flow ve sunucu doğrulaması yok.
- **Instrumented/UI testleri** — birim testleri `:data`'da yoğunlaşıyor; ViewModel ve Compose
  testleri yazılmadı. Sınırlı sürede en çok riski taşıyan yeri (sayfalama, hata eşleme, kota)
  test etmeyi tercih ettim.
- **Room migration'ları** — şema dosyaları repoya yazılıyor ama yazılmış migration yok;
  veritabanı `fallbackToDestructiveMigration(dropAllTables = true)` ile kuruluyor. Geliştirme
  sırasında şema 4 kez değişti ve hepsinde cache sıfırlandı; yayına çıkacak olsaydı ilk iş
  bu olurdu (favoriler kullanıcı verisi, silinmemeli).
- **Çoklu dil** — arayüz metinleri kaynak dosyalarında, içerik ise `langRestrict=en` nedeniyle
  İngilizce. İkisini birlikte lokalize etmek ayrı bir iş.
- **Kota gece yarısı sıfırlanması** — uygulama açıkken gün değişirse ekrana anında yansımıyor,
  bir sonraki emisyonu bekliyor.
