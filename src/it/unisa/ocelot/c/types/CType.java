package it.unisa.ocelot.c.types;

public class CType {

    @Override
    public String toString() {
        String message = null;

        if (this instanceof CPointer) {
            message = "Pointer";
        }

        if (this instanceof CStruct) {
            message = "Struct";
        }

        if (this instanceof CPrimitive) {
            message = "Primitive type";
        }

        return message;
    }
}
