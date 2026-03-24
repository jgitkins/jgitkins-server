package io.jgitkins.server.infrastructure.config.git.hook.push;

import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.port.in.PushEventHandleUseCase;
import io.jgitkins.server.application.port.out.PushEventRequestResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Push-side hook that logs generic push info and branch creation events.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PushHook implements PostReceiveHook {

    private final PushEventHandleUseCase pushEventHandleUseCase;
    private final PushEventRequestResolver pushEventRequestResolver;
    private final PushEventCommandMapper pushEventCommandMapper;

    @Override
    public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
        String gitDirPath = receivePack.getRepository().getDirectory().getAbsolutePath();
        Long requesterId = pushEventRequestResolver.resolveRequesterId().orElse(null);

        log.debug("push event: user=[{}] bare repo path=[{}]", requesterId, gitDirPath);

        // TODO: 헥사고날 관점의 질문 PushHook 은 infrastructure계층의 config 개념인데
        //  `pushEventCommandMapper` 는 application 인지,, infrastructure 인지 불명확 그리고 내부에서 application 의 outgoing port 를 의존하는데 이렇게 사용하는게 이상적일까?
        java.util.Optional<PushEventCommand> pushEventCommand = pushEventCommandMapper.map(gitDirPath, requesterId, commands);
        if (pushEventCommand.isEmpty()) {
            log.debug("push hook skipped: no applicable branch command found");
            return;
        }

        pushEventHandleUseCase.handle(pushEventCommand.get());
    }
}
