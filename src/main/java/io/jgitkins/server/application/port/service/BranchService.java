package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.command.BranchCreateCommand;
import io.jgitkins.server.application.dto.BranchInfo;
import io.jgitkins.server.application.port.in.BranchCreationUseCase;
import io.jgitkins.server.application.port.in.BranchDeletetionUseCase;
import io.jgitkins.server.application.port.in.BranchLoadUseCase;
import io.jgitkins.server.application.port.out.BranchPersistenceCommandPort;
import io.jgitkins.server.application.port.out.CreateBranchPort;
import io.jgitkins.server.application.port.out.DeleteBranchPort;
import io.jgitkins.server.application.port.out.BranchGitLoadPort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
//@RequiredArgsConstructor
public class BranchService implements BranchLoadUseCase, BranchCreationUseCase, BranchDeletetionUseCase {

    private final BranchGitLoadPort branchGitLoadPort;
    private final CreateBranchPort createBranchPort;
    private final DeleteBranchPort deleteBranchPort;
    private final BranchPersistenceCommandPort branchPersistenceCommandPort;

    public BranchService(BranchGitLoadPort branchGitLoadPort, CreateBranchPort createBranchPort, DeleteBranchPort deleteBranchPort, BranchPersistenceCommandPort branchPersistenceCommandPort) {
        this.branchGitLoadPort = branchGitLoadPort;
        this.createBranchPort = createBranchPort;
        this.deleteBranchPort = deleteBranchPort;
        this.branchPersistenceCommandPort = branchPersistenceCommandPort;
    }

    @Override
    public List<BranchInfo> getBranches(String taskCd, String repoName) throws IOException {
        return branchGitLoadPort.getBranches(taskCd, repoName);
    }

    @Override
    public void createBranch(BranchCreateCommand command) throws IOException {
        boolean branchExists = branchGitLoadPort.getBranch(command.getTaskCd(), command.getRepoName(), command.getBranchName()).isPresent();

//        if (command.isPhysicalCreationRequired()) {
//            if (branchExists) {
//                throw new ConflictException(ErrorCode.BRANCH_ALREADY_EXISTS, "Branch Already Exist");
//            }
//            createBranchPort.createBranch(command);
//        }
//
//        branchPersistencePort.create(command.getTaskCd(), command.getRepoName(), command.getBranchName());
    }

    @Override
    public void deleteBranch(String taskCd, String repoName, String branchName) throws IOException {
        deleteBranchPort.deleteBranch(taskCd, repoName, branchName);
//        branchPersistencePort.deleteBranch(taskCd, repoName, branchName);
    }
}
