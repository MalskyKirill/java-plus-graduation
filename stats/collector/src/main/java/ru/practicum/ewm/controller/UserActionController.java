package ru.practicum.ewm.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.grpc.stats.controller.UserActionControllerGrpc;
import ru.practicum.ewm.grpc.stats.event.UserActionProto;
import ru.practicum.ewm.hanler.UserActionHandler;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionHandler handler;

    @Override
    public void collectUserAction(final UserActionProto userActionProto, final StreamObserver<Empty> responseObserver) {
        try {
            log.info("Полученно действие пользователя: userId = {}, eventId = {}, actionType = {}, timestamp = {}",
                userActionProto.getUserId(),
                userActionProto.getEventId(),
                userActionProto.getActionType(),
                userActionProto.getTimestamp());
            log.debug("Действие пользователя = {}", userActionProto);
            handler.handle(userActionProto);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                Status.INTERNAL.withDescription(e.getLocalizedMessage()).withCause(e)));
        }
    }
}


