import java.time.OffsetDateTime
import java.util.*

//

object ClientFileFixtures {

    fun creationEvent() = CreationEvent(
        clientFileId = UUID.randomUUID(),
        clientFileEventId = UUID.randomUUID(),
        dateTime = OffsetDateTime.now(),
        operatorId = UUID.randomUUID(),
        participant = CreationEvent.Participant(
            participantId = UUID.randomUUID(),
            name = "Jean",
            email = "jean@test.com",
        ),
        tasks = listOf(
            CreationEvent.Task(
                name = "cni",
                type = ClientFile.Task.Type.DOCUMENT.name,
            ),
            CreationEvent.Task(
                name = "contract",
                type = ClientFile.Task.Type.SIGNATURE.name,
            )
        )
    )

    fun `given CNI and contract CLientFile`() = creationEvent().clientFile()
    fun CreationEvent.clientFile() = ClientFile.create(this, MutableFetcher.Stub)
}