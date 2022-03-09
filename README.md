**Hensikt**

Syfo-tilgangskontroll er en felles mikrotjeneste for å sikre api'ene til sykefraværs-appene i fagsystem-sonen.
I korte trekk gjør den oppslag mot Microsoft GraphAPI for å finne ut hvilke roller den innloggede veilederen har,
og så sier ja eller nei til om veilederen har tilgang å bruke REST-endepunktet ut i fra det. Om veilederen prøver å få
tilgang til informasjon om en person sjekkes det om personen er diskresjonsmerket, egen ansatt eller tilhører en annen NAV-enhet o.l.

**Bygg og deploy**

*Pipeline*
 Pipeline er på Github Action.
 Commits til Master-branch deployes automatisk til dev-fss og prod-fss.
 Commits til ikke-master-branch bygges uten automatisk deploy.

*Lokalt*

- Bygg ved å kjøre `./gradlew build` i kommandolinjen
- Start ved å kjøre `LocalApplication`

### Lint

Kjør `./gradlew --continue ktlintCheck`

**Redis Cache**

Syfo-tilgangskontroll bruker redis for cache.
Redis pod har en egen pipeline som kjøres ved endringer knyttet til Redis. Redis pod kan også startes manuelt ved å kjøre følgdende kommando: `kubectl apply -f .nais/redis-config.yaml`.
