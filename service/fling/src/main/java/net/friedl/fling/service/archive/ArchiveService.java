package net.friedl.fling.service.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Interface for persisting artifacts
 * 
 * @author Armin Friedl <dev@friedl.net>
 */
public interface ArchiveService {
  /**
   * Retrieve an artifact from the archive
   *
   * @param id The artifact id
   * @return An {@link InputStream} for reading the artifact
   */
  InputStream getArtifact(UUID artifactId) throws IOException;
  
  /**
   * Retrieve a packaged fling from the archive
   * 
   * @param flingId The fling id
   * @return An {@link InputStream} representing the fling and its artifacts
   */
  InputStream getFling(UUID flingId) throws IOException;

  /**
   * Store an artifact
   *
   * @param artifactStream The artifact to store represented as {@link InputStream}
   * @param artifactId The id of the artifact. Must be an existing artifact in the DB. Not null.
   */
  void storeArtifact(UUID artifactId, InputStream artifactStream) throws IOException;

  /**
   * Delete an artifact
   *
   * @param id The unique artifact id 
   */
  void deleteArtifact(UUID artifactId) throws IOException;
  
  /**
   * Delete a fling
   * 
   * @param flingId The unique fling id
   */
  void deleteFling(UUID flingId) throws IOException;
}
