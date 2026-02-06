package com.interviewprep.service;

import com.interviewprep.dto.SessionSummaryResponse;
import com.interviewprep.entity.InterviewSession;
import com.interviewprep.entity.SessionQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF Export Service for interview session reports.
 * Uses iText7 for PDF generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generate PDF report for an interview session.
     * Returns byte array of PDF content.
     */
    public byte[] generateSessionReport(InterviewSession session, List<SessionQuestion> questions, SessionSummaryResponse summary) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

            // Title
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph("Interview Session Report")
                    .setFontSize(20)
                    .setBold()
                    .setMarginBottom(20);
            document.add(title);

            // Session Info
            document.add(new com.itextpdf.layout.element.Paragraph("Session Information:")
                    .setBold()
                    .setMarginTop(10));
            document.add(new com.itextpdf.layout.element.Paragraph("Type: " + session.getType().name()));
            document.add(new com.itextpdf.layout.element.Paragraph("Topic: " + (session.getTopic() != null ? session.getTopic() : "N/A")));
            document.add(new com.itextpdf.layout.element.Paragraph("Total Score: " + 
                    (session.getTotalScore() != null ? String.format("%.2f/10", session.getTotalScore()) : "N/A")));
            
            if (session.getStartTime() != null) {
                document.add(new com.itextpdf.layout.element.Paragraph("Start Time: " + 
                        session.getStartTime().format(DATE_FORMATTER)));
            }
            if (session.getEndTime() != null) {
                document.add(new com.itextpdf.layout.element.Paragraph("End Time: " + 
                        session.getEndTime().format(DATE_FORMATTER)));
            }

            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            // Questions and Answers
            document.add(new com.itextpdf.layout.element.Paragraph("Questions & Answers:")
                    .setBold()
                    .setMarginTop(10));

            for (int i = 0; i < questions.size(); i++) {
                SessionQuestion q = questions.get(i);
                
                document.add(new com.itextpdf.layout.element.Paragraph(
                        String.format("Question %d (%s):", i + 1, q.getType().name()))
                        .setBold()
                        .setMarginTop(15));
                
                document.add(new com.itextpdf.layout.element.Paragraph(q.getQuestionText())
                        .setMarginBottom(5));

                if (q.getUserAnswer() != null && !q.getUserAnswer().trim().isEmpty()) {
                    document.add(new com.itextpdf.layout.element.Paragraph("Your Answer:")
                            .setBold()
                            .setMarginTop(5));
                    document.add(new com.itextpdf.layout.element.Paragraph(q.getUserAnswer())
                            .setMarginBottom(5));
                }

                if (q.getScore() != null) {
                    document.add(new com.itextpdf.layout.element.Paragraph(
                            String.format("Score: %.2f/10", q.getScore()))
                            .setBold()
                            .setMarginTop(5));
                }

                if (q.getAiFeedback() != null && !q.getAiFeedback().trim().isEmpty()) {
                    document.add(new com.itextpdf.layout.element.Paragraph("Feedback:")
                            .setBold()
                            .setMarginTop(5));
                    document.add(new com.itextpdf.layout.element.Paragraph(q.getAiFeedback())
                            .setMarginBottom(10));
                }

                if (q.getType() == SessionQuestion.QuestionType.CODING && q.getCodeInput() != null) {
                    document.add(new com.itextpdf.layout.element.Paragraph("Code Submitted:")
                            .setBold()
                            .setMarginTop(5));
                    document.add(new com.itextpdf.layout.element.Paragraph(q.getCodeInput())
                            .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                                    com.itextpdf.io.font.constants.StandardFonts.COURIER))
                            .setFontSize(9)
                            .setMarginBottom(5));
                }
            }

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
