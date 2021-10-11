/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.jmbnetty.common.handler;

import net.jmb19905.jmbnetty.common.crypto.Encryption;

public interface IEncryptedHandler {
    Encryption getEncryption();
}
