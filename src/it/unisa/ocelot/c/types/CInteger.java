package it.unisa.ocelot.c.types;

public class CInteger extends CPrimitive {
    private boolean signedFlag;
    private boolean shortFlag;
    private boolean longFlag;

    public CInteger () {
        this.longFlag = false;
        this.shortFlag = false;
        this.signedFlag = true;
    }
}
