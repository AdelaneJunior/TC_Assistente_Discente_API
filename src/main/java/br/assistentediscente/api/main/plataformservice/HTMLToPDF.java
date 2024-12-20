package br.assistentediscente.api.main.plataformservice;

import br.assistentediscente.api.integrator.exceptions.files.ErrorCouldNotCreateFile;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HTMLToPDF {


    private static final String PDF_FILE_SUFFIX = ".pdf";

    public static String generate(String htmlString,Path folderPath,String filePrefix) throws ErrorCouldNotCreateFile {
        String pdfPath = getFileName(folderPath, filePrefix);
        try  {
            Tidy tidy = new Tidy();
            tidy.setXHTML(true);
            tidy.setShowWarnings(false);
            tidy.setQuiet(true);
            tidy.setInputEncoding("UTF-8");
            tidy.setOutputEncoding("UTF-8");

            InputStream htmlInputStream = new ByteArrayInputStream(
                    htmlString.getBytes(StandardCharsets.UTF_8));
            Document xhtmlDoc = tidy.parseDOM(htmlInputStream, null);

            OutputStream pdfOutputStream = new FileOutputStream(pdfPath);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(xhtmlDoc, null);
            renderer.layout();
            renderer.createPDF(pdfOutputStream);
            pdfOutputStream.close();
            return pdfPath;
        } catch (Exception e) {
            throw new ErrorCouldNotCreateFile(new Object[]{e.getMessage()});
        }
    }

    private static String getFileName(Path folderPath, String filePrefix) {
        try {
            File dir = folderPath.toFile();
            if (!dir.exists()) {
                if(!dir.mkdirs()){
                    throw new RuntimeException("Couldn't create folder " + dir.getAbsolutePath());
                }
            }

            Pattern pattern = Pattern.compile(filePrefix + "(\\d+)" + PDF_FILE_SUFFIX);

            int maxNumber = getMaxNumber(dir, pattern);

            return Paths.get(dir.getPath(), filePrefix + (maxNumber + 1) + PDF_FILE_SUFFIX).toString();

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int getMaxNumber(File dir, Pattern pattern) {
        int maxNumber = 0;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                Matcher matcher = pattern.matcher(fileName);

                if (matcher.matches()) {
                    int fileNumber = Integer.parseInt(matcher.group(1));

                    if (fileNumber > maxNumber) {
                        maxNumber = fileNumber;
                    }
                }
            }
        }
        return maxNumber;
    }


}
