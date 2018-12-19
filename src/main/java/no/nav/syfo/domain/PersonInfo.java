package no.nav.syfo.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class PersonInfo {
    private final String diskresjonskode;
    private final String geografiskTilknytning;

}
