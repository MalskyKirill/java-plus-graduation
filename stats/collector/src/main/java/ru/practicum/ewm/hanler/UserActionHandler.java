package ru.practicum.ewm.hanler;

import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.ewm.grpc.stats.event.UserActionProto;

public interface UserActionHandler {
    void handle(UserActionProto userActionProto);
}
