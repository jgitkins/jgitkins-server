package io.jgitkins.server.infrastructure.support;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.common.exception.JgitkinsException;
import lombok.experimental.UtilityClass;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class RepositoryFileSystemHelper {

    public void createRepositoryDir(File gitDir) {
        if (gitDir.exists()) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_ALREADY_EXISTS,
                    "Repository already exists: " + gitDir.getAbsolutePath());
        }
        if (!gitDir.mkdirs() && !gitDir.exists()) {
            throw new JgitkinsException(io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode.REPOSITORY_CREATE_FAILED,
                    "Failed to create directories: " + gitDir.getAbsolutePath());
        }
    }

    public void initializeBareRepository(Repository repo) throws IOException {
        repo.create(true);
        repo.getConfig().setBoolean("http", null, "receivepack", true);
        repo.getConfig().save();
    }

    public void deleteRecursively(File target) throws IOException {
        if (target == null || !target.exists()) {
            return;
        }
        File[] contents = target.listFiles();
        if (contents != null) {
            for (File child : contents) {
                deleteRecursively(child);
            }
        }
        if (!target.delete()) {
            throw new IOException("Failed to delete " + target.getAbsolutePath());
        }
    }
}
