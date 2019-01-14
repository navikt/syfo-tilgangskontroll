package no.nav.syfo.domain;

import java.util.Objects;

public class PersonInfo {
    private final String diskresjonskode;
    private final String geografiskTilknytning;

    public PersonInfo(String diskresjonskode, String geografiskTilknytning) {
        this.diskresjonskode = diskresjonskode;
        this.geografiskTilknytning = geografiskTilknytning;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public String getGeografiskTilknytning() {
        return geografiskTilknytning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonInfo that = (PersonInfo) o;
        return Objects.equals(diskresjonskode, that.diskresjonskode) &&
                Objects.equals(geografiskTilknytning, that.geografiskTilknytning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diskresjonskode, geografiskTilknytning);
    }
}
