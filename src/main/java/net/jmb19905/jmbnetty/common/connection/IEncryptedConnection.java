package net.jmb19905.jmbnetty.common.connection;

import net.jmb19905.jmbnetty.common.crypto.Encryption;

public interface IEncryptedConnection extends IConnection{
    Encryption getEncryption();
}
