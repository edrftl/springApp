package org.example.exeptions;

import java.io.Serial;

public class InvoiceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvoiceNotFoundException() {
        super();
    }

    public InvoiceNotFoundException(String customMessage) {
        super(customMessage);
    }
}
