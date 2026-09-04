# Doğrulama sonucu

- Tarih: 2026-09-04
- Java: Oracle JDK 21.0.5 (arm64)
- Maven: 3.9.9
- Spring Boot: 3.5.13
- `mvn verify`: **BUILD SUCCESS**
- **32 test, 0 failure, 0 error, 0 skipped**

| Test sınıfı | Test sayısı |
|---|---:|
| ItemServiceTest | 16 |
| ItemControllerTest | 13 |
| ItemTest | 2 |
| ApplicationIntegrationTest | 1 |

Bağımlılıklar çalışma alanındaki ayrı Maven önbelleğine indirildi. Derleme JDK 21 ile yapıldı. İlk derlemede test JSON metinlerinin kaçış karakterleri düzeltildi; yukarıdaki sonuç düzeltilmiş final kaynakların `verify` çalıştırmasına aittir.

Paketlenen `target/stream-patterns-1.0.0.jar`, Java 21 ile 18080 portunda başlatıldı. `python3 scripts/smoke.py http://localhost:18080` başarılı: beş endpointin gerçek JSON yanıtları ve üç geçersiz isteğin HTTP 400 davranışı doğrulandı. Doğrulama sonrası sunucu durduruldu.

Tekrar etmek için README'deki derleme/çalıştırma adımlarını uygulayın; çalışan uygulama üzerinde `python3 scripts/smoke.py` komutu varsayılan 8080 portunu kullanır. Python sadece bu ek smoke kontrolü için gereklidir; Maven testleri ve uygulama Python gerektirmez.

Sayısal test kapsamı (coverage yüzdesi) ölçülmedi; raporlanan değer çalıştırılan test sayısıdır.
