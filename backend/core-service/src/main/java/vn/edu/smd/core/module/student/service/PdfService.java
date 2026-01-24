package vn.edu.smd.core.module.student.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import vn.edu.smd.core.module.student.dto.StudentSyllabusDetailDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    public byte[] generateSyllabusPdf(StudentSyllabusDetailDto data) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Context context = new Context();
            context.setVariable("syllabus", data);

            String html = templateEngine.process("syllabus-pdf", context);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            String fontFamily = "TimesVN";

            // Nạp font bằng InputStream thay vì File
            addFont(builder, "fonts/TIMES.TTF", fontFamily);
            addFont(builder, "fonts/TIMESBD.TTF", fontFamily);
            addFont(builder, "fonts/TIMESI.TTF", fontFamily);
            addFont(builder, "fonts/TIMESBI.TTF", fontFamily);

            builder.withHtmlContent(html, "");
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            System.err.println("❌ PDF Generation Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private void addFont(PdfRendererBuilder builder, String path, String fontFamily) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                builder.useFont(() -> {
                    try {
                        return resource.getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, fontFamily);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}