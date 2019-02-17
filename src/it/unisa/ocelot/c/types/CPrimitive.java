package it.unisa.ocelot.c.types;

public class CPrimitive extends CType {
    private String name;

    public CPrimitive (String pName) {
        this.name = pName;
    }

    public String getName() {
        return this.name;
    }
}
