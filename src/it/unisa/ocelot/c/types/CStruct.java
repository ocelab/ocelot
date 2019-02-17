package it.unisa.ocelot.c.types;


public class CStruct extends CType {
    private String name;
    private String nameOfStruct;
    private String typedefName;
    private CType [] structParameters;

    public CStruct (String pName, String pNameOfStruct, String pTypedefName) {
        this.name = pName;
        this.nameOfStruct = pNameOfStruct;
        this.typedefName = pTypedefName;
        this.structParameters = null;
    }

    public CStruct (String pName, String pNameOfStruct, String pTypedefName, CType [] pStructParameters) {
        this.name = pName;
        this.nameOfStruct = pNameOfStruct;
        this.typedefName = pTypedefName;
        this.structParameters = pStructParameters;
    }

    public String getName() {
        return name;
    }

    public String getNameOfStruct() {
        return this.nameOfStruct;
    }

    public String getTypedefName() { return typedefName; }

    public CType [] getStructVariables() {
        return structParameters;
    }

    @Override
    public boolean equals(Object obj) {
        CStruct cStructObj = (CStruct) obj;

        if (this.nameOfStruct.equals(cStructObj.nameOfStruct))
            return true;

        return false;
    }
}
