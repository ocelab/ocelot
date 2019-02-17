package it.unisa.ocelot.c.types;

public class CDouble extends CPrimitive {
    private boolean longFlag;

    public CDouble (String pName) {
        super(pName);
        this.longFlag = false;
    }
}
