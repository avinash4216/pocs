package com.java.suports;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <dependency>
 *             <groupId>commons-io</groupId>
 *             <artifactId>commons-io</artifactId>
 *             <version>2.11.0</version>
 *         </dependency>
 *
 * <dependency>
 *             <groupId>org.slf4j</groupId>
 *             <artifactId>slf4j-api</artifactId>
 *             <version>1.7.5</version>
 *         </dependency>
 *
 *         <!-- log4j binding -->
 *         <dependency>
 *             <groupId>org.slf4j</groupId>
 *             <artifactId>slf4j-log4j12</artifactId>
 *             <version>1.7.5</version>
 *         </dependency>
 *
 *         <dependency>
 *             <groupId>org.projectlombok</groupId>
 *             <artifactId>lombok</artifactId>
 *             <version>1.18.6</version>
 *             <scope>provided</scope>
 *         </dependency>
 */
@Slf4j
public class DownloadZipFileAndRead {

    private static final int MAX_NO_OF_RECORDS = 10;

    @Test
    public void tempDirectory() {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        System.out.println(tempDirPath);
    }

    @Test
    @SneakyThrows
    public void main() {
        // create temp directory
        File tempDirectoryFile = createTempDirectory();

        // prepare URL to download file
        // String urlString = "https://www.irs.gov/charities-non-profits/tax-exempt-organization-search-bulk-data-downloads";
        String urlString = "https://apps.irs.gov/pub/epostcard/data-download-epostcard.zip";
        URL url = new URL(urlString);

        // destination file path with file name
        String zipFileDownloadFilePath = tempDirectoryFile.getAbsolutePath() + File.separator + "file.zip";

        // download file
        downloadFileV2(url, zipFileDownloadFilePath);

        // read zip file
        List<String[]> rows = new ArrayList<>();
        readZipFile(zipFileDownloadFilePath, rows);
        log.info("number of rows: {}", rows.size());

        // print rows
        printRows(rows);

        // delete temp directory
        deleteTempDirectory(tempDirectoryFile);
    }

    private void readZipFile(String zipFileDownloadFilePath, List<String[]> rows) throws IOException {
        // read zip file
        log.info("--------------------- Reading zip file started ---------------------------------------------------");
        int i = 1;
        try(ZipFile zipFile = new ZipFile(zipFileDownloadFilePath)){
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while(zipEntries.hasMoreElements()){
                ZipEntry zipEntry = zipEntries.nextElement();
                log.info("zip-file-name: {}", zipEntry.getName());
                try(InputStream is = zipFile.getInputStream(zipEntry); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))){
                    String line = null;
                    while((line = bufferedReader.readLine()) != null){
                        log.info("line: {}", line);
                        rows.add(line.split("\\|"));
                        // FIXME - remove this code before check-in - added for local testing to limit the no.of rows read from file
                        i++;
                        if(i == MAX_NO_OF_RECORDS)
                            break;
                    }
                }
            }
        }
        log.info("--------------------- Reading zip file completed ---------------------------------------------------");
    }

    @SneakyThrows
    private File createTempDirectory(){
        log.info("creating temp directory");
        Path tempDirectory = Files.createTempDirectory("irs_");
        File file = tempDirectory.toFile();
        log.info("temp directory created. path: {}", file.getAbsolutePath());
        return file;
    }

    @SneakyThrows
    private void deleteTempDirectory(File file){
        String fileAbsolutePath = file.getAbsolutePath();
        log.info("deleting directory: {}", fileAbsolutePath);
        FileUtils.deleteDirectory(file);
        log.info("deleted directory: {}", fileAbsolutePath);

    }

    @SneakyThrows
    private void downloadFileV2(URL url, String zipFileDownloadFilePath){
        log.info("downloading file, url: {}, zip-file: {}", url.toString(), zipFileDownloadFilePath);
        FileUtils.copyURLToFile(url, new File(zipFileDownloadFilePath));
        log.info("file downloaded, url: {}, zip-file: {}", url.toString(), zipFileDownloadFilePath);
    }

    private void printRows(List<String[]> rows){
        rows.forEach(row -> {
            String rowString = Arrays.deepToString(row);
            System.out.println(rowString);
        });
    }

    private String downloadFileV1() throws IOException {
        // String urlString = "https://www.irs.gov/charities-non-profits/tax-exempt-organization-search-bulk-data-downloads";
        String urlString = "https://apps.irs.gov/pub/epostcard/data-download-epostcard.zip";
        URL url = new URL(urlString);
        log.info("url.toString: {}", url.toString());

        // temp directory - C:\Users\donthuav\AppData\Local\Temp
        String tempDirPath = System.getProperty("java.io.tmpdir");
        log.info("temp-directory: {}", tempDirPath);

        // download directory - C:\Users\donthuav\AppData\Local\Temp\irs
        String downloadDirectory = tempDirPath + "irs";
        File file = new File(downloadDirectory);
        boolean isIrsDirectoryCreated = file.mkdir();
        log.info("isIrsDirectoryCreated: {}", isIrsDirectoryCreated);

        String zipFileDownloadFilePath = tempDirPath + "irs" + File.separator + "file.zip";
        log.info("zipFileDownloadFilePath: {}", zipFileDownloadFilePath);
        // File zipFile = new File(zipFileDownloadFilePath);

        // download
//        try (InputStream is = url.openStream()) {
//            Files.copy(is, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        }

        // using apache-commons-io library
        FileUtils.copyURLToFile(url, new File(zipFileDownloadFilePath));
        return zipFileDownloadFilePath;
    }

}
