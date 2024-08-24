package org.example.service.impl;

import org.example.exeptions.InvoiceNotFoundException;
import org.example.model.Invoice;
import org.example.repo.InvoiceRepository;
import org.example.service.IInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceServiceImpl implements IInvoiceService {

    @Autowired
    private InvoiceRepository repo;

    @Override
    public Invoice saveInvice(Invoice invoice) {
        return repo.save(invoice);
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return repo.findAll();
    }

    @Override
    public Invoice getInvoiceById(Long id) {
        Optional<Invoice> opt = repo.findById(id);
        if(opt.isPresent()) {
            return opt.get();
        } else {
            throw new InvoiceNotFoundException("Invoice with Id : "+id+" Not Found");
        }
    }

    @Override
    public void deleteInvoiceById(Long id) {
        Invoice invoice = getInvoiceById(id); // Отримуємо рахунок за ідентифікатором

        // Видаляємо файл з файлової системи
        deleteFile(invoice.getImageName());

        // Видаляємо рахунок з бази даних
        repo.delete(invoice);
    }

    private void deleteFile(String imageName) {
        try {
            Path filePath = Paths.get("uploads", imageName); // Створюємо шлях до файлу
            Files.deleteIfExists(filePath); // Видаляємо файл, якщо він існує
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + imageName, e);
        }
    }


    @Override
    public void updateInvoice(Invoice invoice) {
        repo.save(invoice);
    }
}