package net.jmb19905.jmbnetty.common.rpc;

import net.jmb19905.util.registry.Type;

public class ProcedureType<P extends Procedure<?, ?>> extends Type<P> {
    
    private final Class<P> procClass;

    public ProcedureType(Class<P> procClass) {
        this.procClass = procClass;
    }

    public Class<P> getProcClass() {
        return procClass;
    }
}
