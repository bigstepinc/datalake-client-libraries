package com.bigstep.datalake;

import org.apache.directory.server.kerberos.shared.crypto.encryption.KerberosKeyFactory;
import org.apache.directory.server.kerberos.shared.keytab.Keytab;
import org.apache.directory.server.kerberos.shared.keytab.KeytabEntry;
import org.apache.directory.shared.kerberos.KerberosTime;
import org.apache.directory.shared.kerberos.codec.types.EncryptionType;
import org.apache.directory.shared.kerberos.components.EncryptionKey;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Created by alex on 10/23/16.
 */
public class KeytabUtil {

    public static void generateKeytab(String principalName, String userPassword, String keytabFile) throws IOException {

        if(userPassword.length()==0)
            throw new IllegalArgumentException("password must not have zero length");


        if(!principalName.contains("@"))
            throw new IllegalArgumentException("principal name must contain realm: principal@realm");

        Keytab keytab = Keytab.getInstance();
        KerberosTime timeStamp = new KerberosTime();

        Set<EncryptionType> ciphers= EnumSet.of(EncryptionType.AES256_CTS_HMAC_SHA1_96);

        Map<EncryptionType, EncryptionKey> keys = KerberosKeyFactory
                .getKerberosKeys(principalName, userPassword, ciphers);

        KeytabEntry keytabEntry = new KeytabEntry(
                principalName,
                1,
                timeStamp,
                (byte) 0,
                keys.get(EncryptionType.AES256_CTS_HMAC_SHA1_96));

        List<KeytabEntry> entry = Arrays.asList(keytabEntry);

        keytab.setEntries(entry);

        keytab.write(new File(keytabFile));

    }

    public static void main(String argv[]) throws IllegalArgumentException, IOException {
        if(argv.length!=3)
        {
            System.err.println("Syntax: genkeytab <principal@realm> <keytab_file_path>");
            System.exit(-1);
        }

        if(!DLFileSystem.checkJCE())
            throw new IOException(DLFileSystem.JCE_ERROR);

        String principalName = argv[1];
        String keytabFile = argv[2];

        System.out.print("Password (not checked for correctness against KDC):");
        String userPassword = String.valueOf(System.console().readPassword());

        generateKeytab(principalName, userPassword, keytabFile);

    }

}
