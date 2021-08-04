package net.jmb19905.common;

public record Version(int major, int minor, int patch, Type type, int typeVersion) {

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
