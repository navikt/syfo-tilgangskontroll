package no.nav.syfo.mocks;

import no.nav.tjeneste.pip.diskresjonskode.DiskresjonskodePortType;
import no.nav.tjeneste.pip.diskresjonskode.meldinger.WSHentDiskresjonskodeBolkRequest;
import no.nav.tjeneste.pip.diskresjonskode.meldinger.WSHentDiskresjonskodeBolkResponse;
import no.nav.tjeneste.pip.diskresjonskode.meldinger.WSHentDiskresjonskodeRequest;
import no.nav.tjeneste.pip.diskresjonskode.meldinger.WSHentDiskresjonskodeResponse;

public class DiskresjonskodeMock implements DiskresjonskodePortType {
    @Override
    public WSHentDiskresjonskodeResponse hentDiskresjonskode(WSHentDiskresjonskodeRequest wsHentDiskresjonskodeRequest) {
        return new WSHentDiskresjonskodeResponse().withDiskresjonskode("1");
    }

    @Override
    public WSHentDiskresjonskodeBolkResponse hentDiskresjonskodeBolk(WSHentDiskresjonskodeBolkRequest wsHentDiskresjonskodeBolkRequest) {
        return null;
    }
}
