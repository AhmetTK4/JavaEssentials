# Gerçek iş senaryolarıyla 5 Java Stream pattern'i

Java 21 • Spring Boot 3.5.13 • Maven • JUnit 5 • Mockito

Medium yazısına temel olacak çalıştırılabilir eğitim projesi. Makale içermez. “En sık kullanılan” ifadesi istatistiksel bir sıralama değildir; seçilen beş pattern yaygın uygulama ihtiyaçlarını ve farklı Stream araçlarını öğretir.

## Çalıştırma

JDK **21** ve Maven **3.6.3+** kurulu olmalı. `JAVA_HOME` JDK 21'i göstermeli; `mvn -version` ile doğrulayın.

```sh
mvn clean verify
mvn spring-boot:run
# Alternatif:
java -jar target/stream-patterns-1.0.0.jar
```

Sunucu: `http://localhost:8080`. Harici servis, veritabanı veya kimlik bilgisi gerekmez.

## Katmanlar

```text
HTTP → ItemController → ItemService → ItemProvider
                                        └─ FakeItemProvider → immutable Item records
                      └─ ItemView / CategorySummary / Availability DTOs → JSON
```

Controller HTTP parametrelerini bağlar ve doğrular. Servis iş kuralları ile Stream işlemlerini içerir. Provider değiştirilebilir veri kaynağı sözleşmesidir; üretimde veritabanı adaptörüyle değiştirilebilir. Fake kaynak uygulamanın çalışması içindir; Mockito mock'ları yalnızca testlerde kullanılır. Constructor injection vardır.

## Beş senaryo

| Endpoint | Pattern | İş ihtiyacı | JSON |
|---|---|---|---|
| GET `/api/items/catalog?maxPrice=300` | filter + map | Satılabilir ürünleri bütçeye göre DTO'ya dönüştürme | array |
| GET `/api/items/inventory` | groupingBy + collectingAndThen + sum/reduce | Kategori bazında stok değerleme | map of objects |
| GET `/api/items/availability` | partitioningBy + mapping | Aktif ürünleri stok durumuna göre ayırma | object with arrays |
| GET `/api/items/top?limit=3` | sorted + limit | Satılabilir en pahalı ilk N ürün | array |
| GET `/api/items/tags` | flatMap + distinct + sorted | İç içe etiketlerden benzersiz filtre seçenekleri | string array |

`toMap` yerine `partitioningBy` seçildi: kategori bazında gruplama zaten map sonucunu gösteriyor, partitioning ise iki iş kümesini tek geçişte üretmeyi öğretiyor. `reduce`, kategori değerlemesine dahil edildi. Para için `double` kullanan `summaryStatistics` yerine `BigDecimal` toplamı tercih edildi.

## Veri ve iş kuralları

Fiyatlar tek para birimindedir: USD. `inventoryValue` birim fiyat × stok toplamıdır; satış geliri değildir. Para iki ondalık haneyle tutulur, kesirli cent reddedilir ve yuvarlama yapılmaz. Stok toplamı `long` olduğundan `int` taşması önlenir.

| ID | Ürün | Kategori | Fiyat | Stok | Aktif |
|---|---|---|---:|---:|---|
| 1 | Laptop | Electronics | 1200.00 | 3 | evet |
| 2 | Mouse | Electronics | 25.50 | 0 | evet |
| 3 | Desk | Furniture | 300.00 | 5 | evet |
| 4 | Chair | Furniture | 150.00 | 2 | hayır |
| 5 | Monitor | Electronics | 300.00 | 4 | evet |
| 6 | Notebook | Stationery | 5.00 | 10 | evet |

Tüm endpointler pasif ürünleri dışlar. Catalog ve top ayrıca sıfır stoklu ürünleri dışlar; etiketler sıfır stoklu aktif ürünlerden de gelir. Catalog ve availability ID artan sırasındadır. Top fiyat azalan, eşit fiyatlarda ID artan sırasındadır. Etiketler büyük/küçük harfe duyarlı olarak tekilleştirilir ve doğal String sırasıyla döner. Kategori anahtarları TreeMap ile sıralıdır; istemci JSON object anahtar sırasına güvenmemelidir.

Provider null olmayan, null eleman içermeyen ve benzersiz ürün ID'li bir snapshot döndürür. Kategori ve etiket tekrarları geçerlidir; etiket tekrarları tekilleştirilir, aynı kategorideki farklı ürünler toplamda korunur. Aynı ürün ID'sinin tekrar gelmesi provider sözleşmesi ihlalidir; servis bunu sessizce birleştirmez. Domain ve fake veri listeleri dışarıdan değiştirilemez.

## Request ve response örnekleri

Bu beş okuma işlemi GET'tir; request body yoktur. Filtreler query parametresidir, response body JSON'dur.

### 1. Bütçeye uygun katalog

```sh
curl -s 'http://localhost:8080/api/items/catalog?maxPrice=300'
```
```json
[
  {"id":3,"name":"Desk","price":300.00},
  {"id":5,"name":"Monitor","price":300.00},
  {"id":6,"name":"Notebook","price":5.00}
]
```

`maxPrice` dahil sınırdır; minimum 0, varsayılan 1000000. Sıfır fiyatlı ürün geçerlidir.

### 2. Kategori stok özeti

```sh
curl -s 'http://localhost:8080/api/items/inventory'
```
```json
{
  "Electronics":{"itemCount":3,"totalUnits":7,"inventoryValue":4800.00},
  "Furniture":{"itemCount":1,"totalUnits":5,"inventoryValue":1500.00},
  "Stationery":{"itemCount":1,"totalUnits":10,"inventoryValue":50.00}
}
```

`itemCount` stok miktarı değil, aktif ürün sayısıdır; sıfır stoklu Mouse sayılır. Pasif Chair sayılmaz.

### 3. Stok durumuna göre ayırma

```sh
curl -s 'http://localhost:8080/api/items/availability'
```
```json
{
  "available":[
    {"id":1,"name":"Laptop","price":1200.00},
    {"id":3,"name":"Desk","price":300.00},
    {"id":5,"name":"Monitor","price":300.00},
    {"id":6,"name":"Notebook","price":5.00}
  ],
  "unavailable":[{"id":2,"name":"Mouse","price":25.50}]
}
```

### 4. En pahalı ilk N ürün

```sh
curl -s 'http://localhost:8080/api/items/top?limit=3'
```
```json
[
  {"id":1,"name":"Laptop","price":1200.00},
  {"id":3,"name":"Desk","price":300.00},
  {"id":5,"name":"Monitor","price":300.00}
]
```

`limit`: 1–100, varsayılan 3. Veri sayısından büyük limit tüm uygun ürünleri döndürür.

### 5. Benzersiz etiketler

```sh
curl -s 'http://localhost:8080/api/items/tags'
```
```json
["accessory","display","home","portable","work"]
```

Monitor içindeki iki `display` ve ürünler arasında tekrar eden `work` bir kez döner. Etiketsiz Notebook işleme uygundur.

### Boş sonuçlar ve hatalar

Kaynak boşsa veya yalnızca pasif ürün içeriyorsa: catalog/top/tags `[]`, inventory `{}`, availability `{"available":[],"unavailable":[]}` döner; HTTP 200.

```sh
curl -i 'http://localhost:8080/api/items/top?limit=0'
curl -i 'http://localhost:8080/api/items/catalog?maxPrice=-1'
curl -i 'http://localhost:8080/api/items/top?limit=abc'
```

Geçersiz değer/tip HTTP 400 üretir. Spring MVC Problem Details açıktır; hata mesajı metni Spring sürümüne bağlıdır. Provider hatası boş başarı sonucuna çevrilmez.

## Test yaklaşımı

- Service: Mockito ile provider izole edilir; sınır fiyat, ücretsiz ürün, pasif/sıfır stok, ondalık doğruluk, tekrarlanan kategori/etiket, sıralama eşitliği, int sınırını aşan stok toplamı, boş veri ve provider hatası test edilir.
- Etkileşimler: başarılı çağrı başına provider bir kez; geçersiz servis girdisinde sıfır çağrı; beklenmeyen etkileşimler reddedilir.
- Controller: `@WebMvcTest`, `MockMvc`, `@MockitoBean` ile JSON şekilleri, parametre bağlama, varsayılanlar, sıra ve HTTP 400 kontrolleri yapılır. Geçersiz HTTP girdisinde servis çağrılmaz.
- Integration: gerçek rastgele portta sunucu başlar; HTTP → controller → service → fake provider zinciri sınanır.
- Domain: savunmacı kopyalama, negatif değerler ve kesirli cent davranışı sınanır.

Test raporları: `target/surefire-reports/`. Ortamdaki doğrulama sonucu için `VERIFICATION.md` dosyasına bakın.

## Tasarım sınırları

Bu küçük in-memory eğitim verisidir. Büyük veri kümelerinde filtre/sıralama/top-N ve uygun aggregation işlemleri veritabanına taşınmalı, sayfalama uygulanmalıdır. `sorted` O(n log n), diğer temel işlemler yaklaşık O(n)'dir. Kategori collector'ü öğretici açıklık için ara listeler üretir. Paralel stream gerektiren ölçülmüş bir ihtiyaç yoktur. Kimlik doğrulama, veri yazma ve kalıcı depolama bu örneğin kapsamında değildir.

## Resmî kaynaklar

- [Spring Boot 3.5 sistem gereksinimleri](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Java 21 Stream API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html)
- [Java 21 Collectors](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Collectors.html)
