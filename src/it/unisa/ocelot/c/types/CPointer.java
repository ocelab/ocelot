package it.unisa.ocelot.c.types;

public class CPointer extends CType {
    private CType type;

    public CPointer (CType pType) {
        this.type = pType;
    }

    public CType getType() {
        return type;
    }
}
