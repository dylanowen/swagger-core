package io.swagger.models.refs;

/**
 * A class the encapsulates logic that is common to RefModel, RefParameter, and RefProperty
 */
public class GenericRef {

    private final RefFormat format;
    private final RefType type;
    private final String ref;
    private final String simpleRef;

    public GenericRef(RefType type, String ref) {
        this.format = computeRefFormat(ref);
        this.type = type;

        validateFormatAndType(format, type);

        if (format == RefFormat.INTERNAL && !ref.startsWith("#/")) {
            /* this is an internal path that did not start with a #/, we must be in some of ModelResolver code
            while currently relies on the ability to create RefModel/RefProperty objects via a constructor call like
            1) new RefModel("Animal")..and expects get$ref to return #/definitions/Animal
            2) new RefModel("http://blah.com/something/file.json")..and expects get$ref to turn the URL
             */
            this.ref = getPrefixForType(type) + ref;
        } else {
            this.ref = ref;
        }

        this.simpleRef = computeSimpleRef(this.ref, format, type);
    }

    private void validateFormatAndType(RefFormat format, RefType type) {
        if(type == RefType.PATH || type == RefType.RESPONSE) {
            if(format == RefFormat.INTERNAL) {
                //PATH AND RESPONSE refs  can only be URL or RELATIVE
                throw new RuntimeException(type + " refs can not be internal references");
            }
        }
    }

    public RefFormat getFormat() {
        return format;
    }

    public RefType getType() {
        return type;
    }

    public String getRef() {
        return ref;
    }

    public String getSimpleRef() {
        return simpleRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GenericRef that = (GenericRef) o;

        if (format != that.format) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (ref != null ? !ref.equals(that.ref) : that.ref != null) {
            return false;
        }
        return !(simpleRef != null ? !simpleRef.equals(that.simpleRef) : that.simpleRef != null);

    }

    @Override
    public int hashCode() {
        int result = format != null ? format.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (ref != null ? ref.hashCode() : 0);
        result = 31 * result + (simpleRef != null ? simpleRef.hashCode() : 0);
        return result;
    }

    private static String computeSimpleRef(String ref, RefFormat format, RefType type) {
        String result = ref;
        //simple refs really only apply to internal refs
        if (format == RefFormat.INTERNAL) {
            String prefix = getPrefixForType(type);
            result = ref.substring(prefix.length());
        }
        return result;
    }

    private static String getPrefixForType(RefType refType) {
        String result;

        switch (refType) {
            case DEFINITION:
                result = RefConstants.INTERNAL_DEFINITION_PREFIX;
                break;
            case PARAMETER:
                result = RefConstants.INTERNAL_PARAMETER_PREFIX;
                break;
            default:
                throw new RuntimeException("No logic implemented for RefType of " + refType);
        }

        return result;
    }

    private static RefFormat computeRefFormat(String ref) {
        RefFormat result = RefFormat.INTERNAL;
        if (ref.startsWith("http")) {
            result = RefFormat.URL;
        } else if (ref.startsWith("#/")) {
            result = RefFormat.INTERNAL;
        } else if (ref.startsWith(".")) {
            //this means that all relative paths must start with ./ or ../
            result = RefFormat.RELATIVE;
        }

        return result;
    }

}
