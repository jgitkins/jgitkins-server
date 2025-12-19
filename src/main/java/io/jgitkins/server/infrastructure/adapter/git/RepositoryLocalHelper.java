package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.InternalServerErrorException;
import lombok.experimental.UtilityClass;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class RepositoryLocalHelper {

    public void createRepositoryDir(File gitDir) {
        if (gitDir.exists()) {
            throw new ConflictException(ErrorCode.REPOSITORY_ALREADY_EXISTS,
                    "Repository already exists: " + gitDir.getAbsolutePath());
        }
        if (!gitDir.mkdirs() && !gitDir.exists()) {
            throw new InternalServerErrorException(ErrorCode.REPOSITORY_CREATE_FAILED,
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
