package com.familyzheng.maven.utils;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Iterator;

public class SignUtils {

    public static String signFile(String inputFilePath, String privateKeyPath, String passphrase) throws IOException, PGPException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // 加载私钥"/path/to/private-key.asc"
        InputStream keyIn;
        if(privateKeyPath == null|| privateKeyPath.isEmpty()){
            keyIn = SignUtils.class.getClassLoader().getResourceAsStream("key.asc");
        }else {
            keyIn = new FileInputStream(privateKeyPath);
        }

        PGPSecretKeyRingCollection secretKeyRingCollection = new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator()
        );

        // 获取私钥
        PGPSecretKey secretKey = null;
        Iterator<PGPSecretKeyRing> keyRingIterator = secretKeyRingCollection.getKeyRings();
        while (keyRingIterator.hasNext()) {
            PGPSecretKeyRing keyRing = keyRingIterator.next();
            Iterator<PGPSecretKey> keyIterator = keyRing.getSecretKeys();
            while (keyIterator.hasNext()) {
                PGPSecretKey key = keyIterator.next();
                if (key.isSigningKey()) {
                    secretKey = key;
                    break;
                }
            }
        }
        if (secretKey == null) {
            throw new IllegalArgumentException("No signing key found.");
        }

        // 提取私钥
        PGPPrivateKey privateKey = secretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(passphrase.toCharArray())
        );

        // 创建签名生成器
        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA256)
                        .setProvider("BC")
        );
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

        // 读取要签名的文件
        InputStream dataIn = new FileInputStream(inputFilePath);
        int ch;
        while ((ch = dataIn.read()) >= 0) {
            signatureGenerator.update((byte) ch);
        }
        dataIn.close();

        // 生成签名
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArmoredOutputStream armoredOut = new ArmoredOutputStream(out);
        signatureGenerator.generate().encode(armoredOut);
        armoredOut.close();
        return out.toString();
    }

    public static String getFileHash(String filePath, String algorithm) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
