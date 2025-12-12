// package io.jgitkins.server.presentation.scheduler;
//
// import io.jgitkins.server.application.port.in.JobDispatchUseCase;
// import lombok.RequiredArgsConstructor;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
//
// /**
//  * Runner pull 방식으로 전환하면서 일시 중단.
//  * gRPC 기반으로 Job을 할당하면 다시 활성화할 수 있다.
//  */
// @Component
// @RequiredArgsConstructor
// public class JobDispatchScheduler {
//
//     private final JobDispatchUseCase jobDispatchUseCase;
//
//     @Scheduled(fixedDelayString = "${job.scheduler.dispatch-interval-ms:5000}")
//     public void dispatchPendingJob() {
//         jobDispatchUseCase.dispatchPendingJob();
//     }
// }
