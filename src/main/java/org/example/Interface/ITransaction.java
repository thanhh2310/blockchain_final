package org.example.Interface;

import java.time.LocalDate;
import java.util.Date;

public interface ITransaction {
    public String getUnitName() ;

    public void setUnitName(String unitName);

    public String getCodeProduct();

    public void setCodeProduct(String codeProduct);

    public String getNameProduct();

    public void setNameProduct(String nameProduct);

    public LocalDate getDateStart();

    public void setDateStart(LocalDate dateStart);

    public LocalDate getDateEnd();

    public void setDateEnd(LocalDate dateEnd);

    String caCalculateTransactionHash();

    String signTransaction();

    Boolean isValidSignature();
}
