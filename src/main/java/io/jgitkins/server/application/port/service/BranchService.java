package io.jgitkins.server.application.port.service;

import lombok.RequiredArgsConstructor;
import io.jgitkins.server.application.dto.BranchCreateCommand;
import io.jgitkins.server.application.dto.BranchInfo;
import io.jgitkins.server.application.port.in.CreateBranchUseCase;
import io.jgitkins.server.application.port.in.DeleteBranchUseCase;
import io.jgitkins.server.application.port.in.LoadBranchUseCase;
import io.jgitkins.server.application.port.out.BranchPersistencePort;
import io.jgitkins.server.application.port.out.CreateBranchPort;
import io.jgitkins.server.application.port.out.DeleteBranchPort;
import io.jgitkins.server.application.port.out.BranchLoadPort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
//@RequiredArgsConstructor
public class BranchService implements LoadBranchUseCase, CreateBranchUseCase, DeleteBranchUseCase {

    private final BranchLoadPort branchLoadPort;
    private final CreateBranchPort createBranchPort;
    private final DeleteBranchPort deleteBranchPort;
    private final BranchPersistencePort branchPersistencePort;

    public BranchService(BranchLoadPort branchLoadPort, CreateBranchPort createBranchPort, DeleteBranchPort deleteBranchPort, BranchPersistencePort branchPersistencePort) {
        this.branchLoadPort = branchLoadPort;
        this.createBranchPort = createBranchPort;
        this.deleteBranchPort = deleteBranchPort;
        this.branchPersistencePort = branchPersistencePort;
    }

    @Override
    public List<BranchInfo> getBranches(String taskCd, String repoName) throws IOException {
        return branchLoadPort.getBranches(taskCd, repoName);
    }

    @Override
    public void createBranch(BranchCreateCommand command) throws IOException {
        boolean branchExists = branchLoadPort.getBranch(command.getTaskCd(), command.getRepoName(), command.getBranchName()).isPresent();

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
