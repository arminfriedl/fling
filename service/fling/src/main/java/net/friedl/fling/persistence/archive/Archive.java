package net.friedl.fling.persistence.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Archive {
    /**
     * Retrieve an artifact from the archive
     *
     * @param id The unique artifact id as returned by {@link Archive#store}
     * @return An {@link InputStream} for reading the artifact
     */
    InputStream get(String id) throws ArchiveException;

    /**
     * Store an artifact
     *
     * @param is The artifact represented as {@link InputStream}
     * @return A unique archive id for the artifact
     * @throws IOException If anything goes wrong while storing the artifact in
     *         the archive
     */
    String store(InputStream is) throws ArchiveException;

    default String store(File file) throws ArchiveException {
        try {
            return store(new FileInputStream(file));
        }
        catch (IOException ex) {
            throw new ArchiveException(ex);
        }
    }

    /**
     * Delete an artifact
     *
     * @param id The unique artifact id as returned by {@link Archive#store}
     */
    void remove(String id) throws ArchiveException;
}
