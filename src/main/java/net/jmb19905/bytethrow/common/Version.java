package net.jmb19905.bytethrow.common;

public class Version {

    private final int major;
    private final int minor;
    private final int patch;
    private final Type type;
    private final int typeVersion;

    public Version(String version){
        String[] parts = version.split("-");
        String[] semanticParts = parts[0].split("\\.");
        this.major = Integer.parseInt(semanticParts[0]);
        this.minor = Integer.parseInt(semanticParts[1]);
        this.patch = Integer.parseInt(semanticParts[2]);

        Type type1 = Type.STABLE;
        int typeVersion1 = 0;
        try {
            if (parts[1].startsWith("alpha")) {
                type1 = Version.Type.ALPHA;
                parts[1] = parts[1].replaceAll("alpha", "");
            } else if (parts[1].startsWith("beta")) {
                type1 = Version.Type.BETA;
                parts[1] = parts[1].replaceAll("beta", "");
            } else if (parts[1].startsWith("rc")) {
                type1 = Version.Type.RELEASE_CANDIDATE;
                parts[1] = parts[1].replaceAll("rc", "");
            }
            typeVersion1 = Integer.parseInt(parts[1]);
        }catch (ArrayIndexOutOfBoundsException e){/*Version is Stable*/}
        this.type = type1;
        this.typeVersion = typeVersion1;
    }

    public Version(int major, int minor, int patch, Type type, int typeVersion){
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.type = type;
        this.typeVersion = typeVersion;
    }

    public boolean isInCompatible(Version version){
        if(this == version){
            return false;
        }if(this.major != version.major){
            return true;
        }if(this.minor != version.minor){
            return true;
        }if(this.type != version.type){
            return true;
        }
        return type != Type.STABLE && this.typeVersion != version.typeVersion;
    }

    @Override
    public String toString() {
        String semanticString = major + "." + minor + "." + patch;
        String typeString = "";
        if(type != Type.STABLE){
            if(type == Type.ALPHA){
                typeString = "alpha";
            }else if(type == Type.BETA){
                typeString = "beta";
            }else if(type == Type.RELEASE_CANDIDATE){
                typeString = "rc";
            }
            typeString = typeString.concat(typeVersion + "");
        }
        return semanticString + "-" + typeString;
    }

    public enum Type{ALPHA,BETA,RELEASE_CANDIDATE,STABLE}

}
