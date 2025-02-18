package ai.zhidun.app.knowledge.store.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public final class FileParser {

    public static final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String CONTENT_TYPE_PDF = "application/pdf";

    public sealed interface ParsedResult permits Unknown, Pdf, Docx {

        String newKey();

        String fileName();

        String contentType();

        InputStream getInputStream();

        default String title() {
            return fileName();
        }
    }

    public sealed interface TextExtractor permits Pdf, Docx {
        String content();

        String fileName();
    }

    public record Pdf(String fileName, String title, String contentType,
                      Supplier<InputStream> supplier) implements TextExtractor, ParsedResult {
        @Override
        public String newKey() {
            return UUID.randomUUID() + ".pdf";
        }

        @Override
        public String content() {
            try (InputStream inputStream = this.getInputStream()) {
                try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(doc);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] renderFirstPageAsJpeg(int dpi) throws IOException {
            try (InputStream inputStream = this.getInputStream()) {
                try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
                    validateDocument(document);

                    PDFRenderer renderer = new PDFRenderer(document);
                    BufferedImage image = renderer.renderImageWithDPI(0, dpi);

                    // 转换到RGB色彩空间（解决JPEG不支持透明度问题）
                    BufferedImage rgbImage = convertToRgb(image);

                    return convertToJpegBytes(rgbImage);
                }
            }
        }

        private static final String JPEG_FORMAT = "JPEG";

        private static byte[] convertToJpegBytes(BufferedImage image) throws IOException {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                if (!ImageIO.write(image, JPEG_FORMAT, baos)) {
                    throw new IOException("JPEG格式转换失败");
                }
                return baos.toByteArray();
            }
        }

        private static BufferedImage convertToRgb(BufferedImage source) {
            BufferedImage rgbImage = new BufferedImage(
                    source.getWidth(),
                    source.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return rgbImage;
        }

        private static void validateDocument(PDDocument doc) throws IOException {
            if (doc.getNumberOfPages() == 0) {
                throw new IOException("PDF文件没有可转换的页面");
            }
        }

        @Override
        public String fileName() {
            return fileName;
        }

        @Override
        public String contentType() {
            return contentType;
        }

        @Override
        public InputStream getInputStream() {
            return supplier.get();
        }
    }

    public record Docx(String fileName, String title, String contentType,
                       Supplier<InputStream> supplier) implements TextExtractor, ParsedResult {
        @Override
        public String newKey() {
            return UUID.randomUUID() + ".docs";
        }

        @Override
        public String content() {
            try (XWPFDocument document = new XWPFDocument(this.getInputStream())) {
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                return extractor.getText();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String fileName() {
            return fileName;
        }

        @Override
        public String contentType() {
            return contentType;
        }

        @Override
        public InputStream getInputStream() {
            return supplier.get();
        }
    }

    public record Unknown(String fileName, String title, String contentType,
                          Supplier<InputStream> supplier) implements ParsedResult {

        @Override
        public String newKey() {
            return UUID.randomUUID() + ".unknown";
        }

        @Override
        public String fileName() {
            return fileName;
        }

        @Override
        public String contentType() {
            return contentType;
        }

        @Override
        public InputStream getInputStream() {
            return supplier.get();
        }
    }

    @SneakyThrows
    public static ParsedResult parse(MultipartFile file) {

        String contentType = file.getContentType();
        if (CONTENT_TYPE_DOCX.equals(contentType)) {
            return new Docx(
                    file.getOriginalFilename(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    () -> toStream(file));
        } else if (CONTENT_TYPE_PDF.equals(contentType)) {
            return new Pdf(
                    file.getOriginalFilename(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    () -> toStream(file));
        } else {
            return new Unknown(
                    file.getOriginalFilename(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    () -> toStream(file));
        }
    }

    @SneakyThrows
    private static InputStream toStream(MultipartFile file) {
        return file.getInputStream();
    }

    @SneakyThrows
    private static InputStream toStream(File file) {
        return new FileInputStream(file);
    }

    private static String contentTypeOf(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return CONTENT_TYPE_PDF;
        } else if (fileName.endsWith(".docx")) {
            return CONTENT_TYPE_DOCX;
        } else {
            return "application/octet-stream";
        }
    }

    @SneakyThrows
    public static ParsedResult parse(File file, String title, String fileName) {

        String contentType = contentTypeOf(fileName);
        if (CONTENT_TYPE_DOCX.equals(contentType)) {
            return new Docx(
                    fileName,
                    title,
                    contentType,
                    () -> toStream(file));
        } else if (CONTENT_TYPE_PDF.equals(contentType)) {
            return new Pdf(
                    fileName,
                    title,
                    contentType,
                    () -> toStream(file));
        } else {
            return new Unknown(
                    fileName,
                    title,
                    contentType,
                    () -> toStream(file));
        }
    }

    private static final String LIBRE_OFFICE_CMD = "libreoffice";

    @SneakyThrows
    public <R> R toPdf(Docx docx, Function<Pdf, R> handler) {
        try (InputStream input = docx.getInputStream()) {
            Path tempInputFile = Files.createTempFile("pdf-output-", ".tmp.docx");
            Files.copy(input, tempInputFile, StandardCopyOption.REPLACE_EXISTING);

            Path path = tempInputFile.toAbsolutePath();

            Path pdf = path
                    .getParent()
                    .resolve(path
                            .getFileName()
                            .toString()
                            .replace(".docx", ".pdf"));

            ProcessBuilder processBuilder = new ProcessBuilder(
                    LIBRE_OFFICE_CMD,
                    "--headless",
                    "--convert-to", "pdf",
                    "--outdir", path.getParent().toString(),
                    path.toString()
            );

            // 执行命令并等待完成
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("转换失败，退出码: " + exitCode);
            }

            R result = handler.apply(new Pdf(
                    docx.fileName.replace(".docx", ".pdf"),
                    docx.title,
                    CONTENT_TYPE_PDF,
                    () -> toStream(pdf.toFile())
            ));

            Files.deleteIfExists(path);
            Files.deleteIfExists(path);

            return result;
        }
    }
}
