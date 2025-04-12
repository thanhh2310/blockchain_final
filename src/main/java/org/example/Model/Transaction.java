package org.example.Model;

import org.example.Interface.ITransaction;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.time.LocalDate;
import java.util.Base64;

public class Transaction implements ITransaction {
    private String unitName;
    private String codeProduct;
    private String nameProduct;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private final long timestamp;
    private KeyPair keyPair;
    private String signature;

    // Khởi tạo cặp khóa cho từng giao dich
    public Transaction(String unitName, String codeProduct, String nameProduct, LocalDate dateStart,
            LocalDate dateEnd, long timestamp) {
        this.unitName = unitName;
        this.codeProduct = codeProduct;
        this.nameProduct = nameProduct;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.timestamp = timestamp;

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo KeyPair: " + e.getMessage());
        }
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getCodeProduct() {
        return codeProduct;
    }

    public void setCodeProduct(String codeProduct) {
        this.codeProduct = codeProduct;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public LocalDate getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDate getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDate dateEnd) {
        this.dateEnd = dateEnd;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String caCalculateTransactionHash() {
        try {
            StringBuilder txnHash = new StringBuilder();
            txnHash.append(unitName)
                    .append(codeProduct)
                    .append(nameProduct)
                    .append(dateStart)
                    .append(dateEnd)
                    .append(timestamp);
            // ma hoa voi thuat toan sha256
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            // chuyen chuoi thanh mang nhi phan roi ma hoa
            byte[] hashBytes = hash.digest(txnHash.toString().getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String signTransaction() {
        try {
            // Get the transaction hash
            String transactionHash = caCalculateTransactionHash();

            // Create signature using private key
            java.security.Signature signature = java.security.Signature.getInstance("SHA256withECDSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(transactionHash.getBytes(StandardCharsets.UTF_8));

            // Sign and encode to Base64
            byte[] signatureBytes = signature.sign();
            this.signature = Base64.getEncoder().encodeToString(signatureBytes);
            return this.signature;
        } catch (Exception e) {
            System.err.println("Error signing transaction: " + e.getMessage());
            return "";
        }
    }

    @Override
    public Boolean isValidSignature() {
        try {
            // lấy giao dịch da duoc bam
            String transactionHash = caCalculateTransactionHash();

            // Giai ma chu ky
            byte[] signatureBytes = Base64.getDecoder().decode(this.signature);

            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(keyPair.getPublic());
            signature.update(transactionHash.getBytes(StandardCharsets.UTF_8));

            return signature.verify(signatureBytes);

        } catch (Exception e) {
            System.err.println("Error verifying signature: " + e.getMessage());
            return false;
        }

    }

}
