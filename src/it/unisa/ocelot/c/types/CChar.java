package it.unisa.ocelot.c.types;

public class CChar extends CPrimitive {
    private boolean signedFlag;

    public CChar(String pName) {
        super(pName);
        this.signedFlag = true;
    }

    public void setSignedFlag (boolean signedFlag) {
        this.signedFlag = signedFlag;
    }
}
