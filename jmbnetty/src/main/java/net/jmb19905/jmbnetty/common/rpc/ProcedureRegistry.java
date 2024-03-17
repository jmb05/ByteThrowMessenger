package net.jmb19905.jmbnetty.common.rpc;

import net.jmb19905.util.Logger;
import net.jmb19905.util.registry.Registry;

public class ProcedureRegistry extends Registry {

    private static final ProcedureRegistry instance = new ProcedureRegistry();

    public <P extends Procedure<?, ?>> void register(String id, Class<P> packetClass) {
        super.register(id, new ProcedureType<>(packetClass));
    }

    public ProcedureType<? extends Procedure<?, ?>> getPacketType(String id) {
        try {
            return (ProcedureType<? extends Procedure<?, ?>>) getRegistry(id);
        } catch (NullPointerException e) {
            Logger.error("No such ProcedureType registered");
            return null;
        }
    }

    public String getId(ProcedureType<? extends Procedure<?, ?>> type) {
        return getAllIds().stream().filter(id -> getRegistry(id) == type).findFirst().orElse("null");
    }

    public static ProcedureRegistry getInstance() {
        return instance;
    }
}
