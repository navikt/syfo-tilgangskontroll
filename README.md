**Hensikt**

Syfo-tilgangskontroll er en felles mikrotjeneste for å sikre api'ene til sykefraværs-appene i fagsystem-sonen.
I korte trekk gjør den oppslag mot LDAP for å finne ut hvilke roller den innloggede veilederen har (Se no.nav.syfo.domain.AdRoller), 
og så sier ja eller nei til om veilederen har tilgang å bruke rest-endepunktet ut i fra det. Om veilederen prøver å få 
tilgang til informasjon om en person sjekkes det om personen er diskresjonsmerket, egen ansatt eller tilhører en annen NAV-enhet o.l.   

Vi har bevisst valgt å ikke bruke ABAC til dette da vi av og til trenger å vise veilederen hvilken rolle han/hun mangler, 
samt at ABAC virker som en unødvendig komplisert abstraksjon for tilgangsstyring. Det er viktig at vi utviklerne 
i Digisyfo har et bevisst forhold til sikkerheten i appene våre, og da er det også viktig at det er lett for oss å forstå og verifisere 
at den er god nok.

**Bygg og deploy**

*Lokalt*

- Bygg med `mvn clean install`
- Start ved å kjøre `LocalApplication`  

*Preprod og prod*

Bygges og deployes av Jenkins:
https://jenkins-digisyfo.adeo.no/job/digisyfo/job/syfo-tilgangskontroll/

**Redis Cache**

Syfo-tilgangskontroll bruker redis for cache.
Redis pod må startes manuelt ved å kjøre følgdende kommando: `kubectl apply -f redis-config.yaml`.
