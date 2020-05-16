package net.friedl.fling.persistence.archive.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.friedl.fling.persistence.archive.Archive;
import net.friedl.fling.persistence.archive.ArchiveException;

@Component("fileSystemArchive")
public class FileSystemArchive implements Archive {
    private MessageDigest fileStoreDigest;

    private FileSystemArchiveConfiguration configuration;

    @Autowired
    public FileSystemArchive(MessageDigest fileStoreDigest, FileSystemArchiveConfiguration configuration) {
        this.fileStoreDigest = fileStoreDigest;
        this.configuration = configuration;
    }

    @Override
    public InputStream get(String id) throws ArchiveException {
        try {
            var path = Paths.get(configuration.getDirectory(), id);
            FileInputStream fis = new FileInputStream(path.toFile());
            return fis;
        }
        catch (FileNotFoundException ex) {
            throw new ArchiveException(ex);
        }
    }

    @Override
    public String store(InputStream is) throws ArchiveException {
        try {
            byte[] fileBytes = is.readAllBytes();
            is.close();

            String fileStoreId = hexEncode(fileStoreDigest.digest(fileBytes));

            FileChannel fc = FileChannel.open(Paths.get(configuration.getDirectory(), fileStoreId),
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            fc.write(ByteBuffer.wrap(fileBytes));

            fc.close();
            return fileStoreId;

        }
        catch (IOException ex) {
            throw new ArchiveException(ex);
        }
    }

    private String hexEncode(byte[] fileStoreId) {
        StringBuilder sb = new StringBuilder(fileStoreId.length * 2);
        for (byte b : fileStoreId)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
