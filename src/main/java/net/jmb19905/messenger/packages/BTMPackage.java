package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;

public abstract class BTMPackage {

    /**
     *
     * Tells the Client what to do when a Package of this type was received
     *
     * @param connection the Connection that the Package was received on
     * @throws UnsupportedSideException if the Package was received on the wrong side
     */
    public abstract void handleOnClient(Connection connection) throws UnsupportedSideException;

    /**
     *
     * Tells the Server what to do when a Package of this type was received
     *
     * @param connection the Connection that the Package was received on
     * @throws UnsupportedSideException if the Package was received on the wrong side
     */
    public abstract void handleOnServer(Connection connection) throws UnsupportedSideException;

}
