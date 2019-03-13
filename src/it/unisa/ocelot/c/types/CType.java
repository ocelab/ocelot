package it.unisa.ocelot.c.types;

public class CType {

    @Override
    public String toString() {
        String message = null;

        if (this instanceof CPointer) {
            message = "Pointer";
        }

        else if (this instanceof CStruct) {
            message = ((CStruct)this).getNameOfStruct();
        }

        else if (this instanceof CInteger) {
            message = "Integer";
        }

        else if (this instanceof CDouble) {
            message = "Double";
        }

        else if (this instanceof CFloat) {
            message = "Float";
        }

        else {
            message = "Char";
        }

        return message;
    }
}
