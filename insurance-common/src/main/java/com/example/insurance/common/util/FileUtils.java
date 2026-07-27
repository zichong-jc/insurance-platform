
package com.example.insurance.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
public final class FileUtils {

    private FileUtils() {}

    public static String generateFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + extension;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static long getFileSize(File file) {
        return file != null && file.exists() ? file.length() : 0;
    }

    public static byte[] readFileToBytes(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File not found: " + (file != null ? file.getPath() : "null"));
        }
        return Files.readAllBytes(file.toPath());
    }

    public static byte[] readFileToBytes(String filePath) throws IOException {
        return readFileToBytes(new File(filePath));
    }

    public static void writeBytesToFile(byte[] content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, content);
    }

    public static void writeBytesToFile(byte[] content, File file) throws IOException {
        Files.createDirectories(file.toPath().getParent());
        Files.write(file.toPath(), content);
    }

    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    public static boolean deleteFile(File file) {
        return file != null && file.delete();
    }

    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public static String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".json")) {
            return "application/json";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerName.endsWith(".xml")) {
            return "application/xml";
        } else if (lowerName.endsWith(".html")) {
            return "text/html";
        }
        return "application/octet-stream";
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}