package it.unisa.ocelot.c.types;


public class CStruct extends CType {
    private String name;
    private CType [] structParameters;

    public CStruct (String pName) {
        this.name = pName;
        this.structParameters = null;
    }

    public CStruct (String pName, CType [] pStructParameters) {
        this.name = pName;
        this.structParameters = pStructParameters;
    }

    public String getName() {
        return name;
    }

    public CType [] getStructVariables() {
        return structParameters;
    }

    @Override
    public boolean equals(Object obj) {
        CStruct cStructObj = (CStruct) obj;

        if (this.name.equals(cStructObj.name))
            return true;

        return false;
    }
}
