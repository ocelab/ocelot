package it.unisa.ocelot.c.types;

public class CFloat extends CPrimitive {
    private boolean longFlag;

    public CFloat (String pName) {
        super(pName);
        this.longFlag = false;
    }
}
