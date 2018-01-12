package no.nav.syfo.domain;

public enum AdRoller {
    KODE6("0000-GA-GOSYS_KODE6"),
    KODE7("0000-GA-GOSYS_KODE7"),
    SYFO("0000-GA-SYFO-SENSITIV"),
    EGEN_ANSATT("0000-GA-GOSYS_UTVIDET");


    public String rolle;


    AdRoller (String rolle) {
        this.rolle = rolle;
    }
}
