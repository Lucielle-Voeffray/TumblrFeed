package services;


import org.bouncycastle.openpgp.PGPSecretKeyRing;

public class Cypher {
    public static void main(String[] args) {
        PGPSecretKeyRing secretKey = PGPainlessCLI.readKeyring();
    }
}
