package org.example.controllers;

import org.example.exeptions.InvoiceNotFoundException;
import org.example.model.Invoice;
import org.example.service.IInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private IInvoiceService service;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/")
    public String showHomePage() {
        return "homePage";
    }

    @GetMapping("/register")
    public String showRegistration() {
        return "registerInvoicePage";
    }

    @PostMapping("/save")
    public String saveInvoice(
            @ModelAttribute Invoice invoice,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) {
        String imageName = uploadFile(imageFile);
        invoice.setImageName(imageName);
        Long id = service.saveInvice(invoice).getId();
        String message = "Record with id : '"+id+"' is saved successfully !";
        model.addAttribute("message", message);
        return "registerInvoicePage";
    }

    @GetMapping("/getAllInvoices")
    public String getAllInvoices(
            @RequestParam(value = "message", required = false) String message,
            Model model
    ) {
        List<Invoice> invoices= service.getAllInvoices();
        model.addAttribute("list", invoices);
        model.addAttribute("message", message);
        return "allInvoicesPage";
    }

    @GetMapping("/edit")
    public String getEditPage(
            Model model,
            RedirectAttributes attributes,
            @RequestParam Long id
    ) {
        String page = null;
        try {
            Invoice invoice = service.getInvoiceById(id);
            model.addAttribute("invoice", invoice);
            page="editInvoicePage";
        } catch (InvoiceNotFoundException e) {
            e.printStackTrace();
            attributes.addAttribute("message", e.getMessage());
            page="redirect:getAllInvoices";
        }
        return page;
    }

    @PostMapping("/update")
    public String updateInvoice(
            @ModelAttribute Invoice invoice,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes attributes
    ) {
        // Отримуємо старий рахунок для доступу до старого імені файлу
        Invoice oldInvoice = service.getInvoiceById(invoice.getId());

        // Перевіряємо, чи є новий файл для завантаження
        if (!imageFile.isEmpty()) {
            // Видаляємо старий файл, якщо він існує
            deleteFile(oldInvoice.getImageName());

            // Завантажуємо новий файл
            String imageName = uploadFile(imageFile);
            invoice.setImageName(imageName);
        } else {
            // Якщо новий файл не було надіслано, залишаємо старе ім'я файлу
            invoice.setImageName(oldInvoice.getImageName());
        }

        // Оновлюємо рахунок
        service.updateInvoice(invoice);
        Long id = invoice.getId();
        attributes.addAttribute("message", "Invoice with id: '" + id + "' is updated successfully !");
        return "redirect:getAllInvoices";
    }

    private void deleteFile(String imageName) {
        if (imageName != null && !imageName.isEmpty()) {
            Path filePath = Paths.get(UPLOAD_DIR + imageName);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @GetMapping("/delete")
    public String deleteInvoice(
            @RequestParam Long id,
            RedirectAttributes attributes
    ) {
        try {
            service.deleteInvoiceById(id);
            attributes.addAttribute("message", "Invoice with Id : '"+id+"' is removed successfully!");
        } catch (InvoiceNotFoundException e) {
            e.printStackTrace();
            attributes.addAttribute("message", e.getMessage());
        }
        return "redirect:getAllInvoices";
    }

    private String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileName;
    }
}
