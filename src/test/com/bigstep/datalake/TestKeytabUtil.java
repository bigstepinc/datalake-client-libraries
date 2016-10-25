package com.bigstep.datalake;

import org.apache.directory.server.kerberos.shared.keytab.Keytab;
import org.apache.directory.server.kerberos.shared.keytab.KeytabEntry;
import org.apache.directory.shared.kerberos.codec.types.EncryptionType;
import org.junit.Test;

import java.io.File;
import java.io.IOException;


import static org.junit.Assert.assertEquals;

public class TestKeytabUtil
{
    @Test
    public void testKeytabGenerator() throws IOException {
        String principalName = "k7@bigstep.io";
        String userPassword="131343";
        String keytabFilePath = "testkeytab";

        com.bigstep.datalake.KeytabUtil ktu = new com.bigstep.datalake.KeytabUtil();

        ktu.generateKeytab(principalName, userPassword, keytabFilePath);

        File keytabFile =new File(keytabFilePath);
        Keytab keytab= Keytab.read(keytabFile);
        KeytabEntry entry=keytab.getEntries().get(0);

        assertEquals(principalName, entry.getPrincipalName());
        assertEquals(EncryptionType.AES256_CTS_HMAC_SHA1_96, entry.getKey().getKeyType());

        keytabFile.delete();
    }
}
