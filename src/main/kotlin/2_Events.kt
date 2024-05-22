import java.time.OffsetDateTime
import java.util.*


sealed interface ClientFileEvent {
    val clientFileEventId: UUID
    val clientFileId: UUID
    val eventNumber: Long
    val dateTime: OffsetDateTime
    val operatorId: UUID
    val type: Type

    enum class Type {
        CREATION,
        DOCUMENT_START,
        DOCUMENT,
        SIGNATURE,
        ACCEPTATION,
        REOPEN,
        UPDATE,
    }

    companion object {
        val SYSTEM = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}

data class CreationEvent(
    override val clientFileEventId: UUID,
    override val clientFileId: UUID,
    override val dateTime: OffsetDateTime,
    override val operatorId: UUID,

    val participant: Participant,
    val tasks: List<Task>,
) : ClientFileEvent {
    override val eventNumber = 1L
    override val type: ClientFileEvent.Type = ClientFileEvent.Type.CREATION

    data class Participant(
        val participantId: UUID,
        val name: String,
        val email: String,
    )

    data class Task(
        val name: String,
        val type: String,
    )
}

data class DocumentStartEvent private constructor(
    override val clientFileEventId: UUID,
    override val clientFileId: UUID,
    override val eventNumber: Long,
    override val dateTime: OffsetDateTime,
    override val operatorId: UUID,

    val taskId: String,
) : ClientFileEvent {
    override val type: ClientFileEvent.Type = ClientFileEvent.Type.DOCUMENT_START

    companion object {
        fun ClientFile.createDocumentStartEvent(operatorId: UUID, taskId: String) = DocumentStartEvent(
            clientFileEventId = UUID.randomUUID(),
            clientFileId = this.clientFileId,
            eventNumber = this.eventCount + 1L,
            dateTime = OffsetDateTime.now(),
            operatorId = operatorId,
            taskId = taskId,
        )
    }
}

data class DocumentEvent private constructor(
    override val clientFileEventId: UUID,
    override val clientFileId: UUID,
    override val eventNumber: Long,
    override val dateTime: OffsetDateTime,
    override val operatorId: UUID,

    val taskId: String,
    val participantNameFound: String,
) : ClientFileEvent {
    override val type: ClientFileEvent.Type = ClientFileEvent.Type.DOCUMENT

    companion object {
        fun ClientFile.createDocumentEvent(
            operatorId: UUID,
            taskId: String,
            participantNameFound: String
        ) = DocumentEvent(
            clientFileEventId = UUID.randomUUID(),
            clientFileId = this.clientFileId,
            eventNumber = this.eventCount + 1L,
            dateTime = OffsetDateTime.now(),
            operatorId = operatorId,
            taskId = taskId,
            participantNameFound = participantNameFound,
        )
    }
}

data class SignatureEvent private constructor(
    override val clientFileEventId: UUID,
    override val clientFileId: UUID,
    override val eventNumber: Long,
    override val dateTime: OffsetDateTime,

    val taskId: String,
) : ClientFileEvent {
    override val operatorId: UUID = ClientFileEvent.SYSTEM
    override val type: ClientFileEvent.Type = ClientFileEvent.Type.SIGNATURE

    companion object {
        fun ClientFile.createSignatureEvent(
            taskId: String,
        ) = SignatureEvent(
            clientFileEventId = UUID.randomUUID(),
            clientFileId = this.clientFileId,
            eventNumber = this.eventCount + 1L,
            dateTime = OffsetDateTime.now(),
            taskId = taskId,
        )
    }
}

data class AcceptationEvent private constructor(
    override val clientFileEventId: UUID,
    override val clientFileId: UUID,
    override val eventNumber: Long,
    override val dateTime: OffsetDateTime,
    override val operatorId: UUID,
) : ClientFileEvent {
    override val type: ClientFileEvent.Type = ClientFileEvent.Type.ACCEPTATION

    companion object {
        fun ClientFile.createAcceptationEvent(
            operatorId: UUID,
        ) = AcceptationEvent(
            clientFileEventId = UUID.randomUUID(),
            clientFileId = this.clientFileId,
            eventNumber = this.eventCount + 1L,
            dateTime = OffsetDateTime.now(),
            operatorId = operatorId,
        )
    }
}

data class AutoAcceptationEvent private constructor(
    override val clientFileEventId: UUID,
    override val clientFileId: UUID,
    override val eventNumber: Long,
    override val dateTime: OffsetDateTime,
) : ClientFileEvent {
    override val operatorId = ClientFileEvent.SYSTEM
    override val type: ClientFileEvent.Type = ClientFileEvent.Type.ACCEPTATION

    companion object {
        fun ClientFile.createAutoAcceptationEvent() =
            AutoAcceptationEvent(
                clientFileEventId = UUID.randomUUID(),
                clientFileId = this.clientFileId,
                eventNumber = this.eventCount + 1L,
                dateTime = OffsetDateTime.now(),
            )
    }
}

data class ReopenEvent private constructor(
    override val clientFileEventId: UUID,
    override val clientFileId: UUID,
    override val eventNumber: Long,
    override val dateTime: OffsetDateTime,
    override val operatorId: UUID,

    val reopenedTasksId: List<String>,
) : ClientFileEvent {
    override val type: ClientFileEvent.Type = ClientFileEvent.Type.REOPEN

    companion object {
        fun ClientFile.createReopenEvent(
            operatorId: UUID,
            reopenedTasks: List<String>,
        ) = ReopenEvent(
            clientFileEventId = UUID.randomUUID(),
            clientFileId = this.clientFileId,
            eventNumber = this.eventCount + 1L,
            dateTime = OffsetDateTime.now(),
            operatorId = operatorId,
            reopenedTasksId = reopenedTasks,
        )
    }
}

data class UpdateEvent private constructor(
    override val clientFileEventId: UUID,
    override val clientFileId: UUID,
    override val eventNumber: Long,
    override val dateTime: OffsetDateTime,
    override val operatorId: UUID,

    val newName: String,
) : ClientFileEvent {
    override val type: ClientFileEvent.Type = ClientFileEvent.Type.UPDATE

    companion object {
        fun ClientFile.createUpdateEvent(
            operatorId: UUID,
            newName: String,
        ) = UpdateEvent(
            clientFileEventId = UUID.randomUUID(),
            clientFileId = this.clientFileId,
            eventNumber = this.eventCount + 1L,
            dateTime = OffsetDateTime.now(),
            operatorId = operatorId,
            newName = newName,
        )
    }
}
