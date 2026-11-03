# ChatPlus (Paper 1.21.x, Java 21)

- Cooldown czatu (domyślnie 5 s) z komunikatem
- `/msg <nick> <wiad>` (`/r` odpowiedź) – format `[Ja -> nick] wiadomość` na niebiesko
- `/ustawienia` – GUI: wiadomości na czacie, prywatne wiadomości, wiadomości o śmierci, automatyczne wiadomości
- `/gift` – GUI: wyślij gifta (offline, wygasa po 72 h), odbierz przedmioty, historia (wycofywanie), powiadomienia
  - `/gift <nick>` wysyła przedmiot z ręki od razu
  - po wysłaniu odbiorca dostaje zielony boss bar „Dostałeś gifta od <nick>”
- `/chatplus reload`

Wszystkie teksty są w `config.yml`.

## Build
GitHub: wrzuć repo → zakładka **Actions** → **Build** → pobierz artefakt `ChatPlus` (plik `ChatPlus.jar`).
Lokalnie: `mvn package` → `target/ChatPlus.jar` → wrzuć do `plugins/`.
