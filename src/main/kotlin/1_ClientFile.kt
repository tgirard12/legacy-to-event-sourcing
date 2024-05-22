import java.time.OffsetDateTime
import java.util.*


data class ClientFile private constructor(
    val clientFileId: UUID,
    val creationDate: OffsetDateTime,

    val participant: Participant,
    val tasks: List<Task>,
    var acceptationDate: OffsetDateTime? = null
) {
    var eventCount: Int = 0

    data class Participant(
        val participantId: UUID,
        val email: String,
        var name: String,
    )

    data class Task(
        val taskId: String,
        val type: Type,
        var state: State,
    ) {
        enum class Type { DOCUMENT, SIGNATURE }
        enum class State { UNAVAILABLE, TODO, IN_PROGRESS, DONE }
    }

    companion object {
        fun create(ev: CreationEvent, fetcher: MutableFetcher): ClientFile {
            return ClientFile(
                clientFileId = ev.clientFileId,
                creationDate = ev.dateTime,
                participant = Participant(
                    participantId = ev.participant.participantId,
                    name = ev.participant.name,
                    email = ev.participant.email,
                ),
                tasks = ev.tasks.map { t ->
                    Task(
                        taskId = t.name,
                        state = Task.State.UNAVAILABLE,
                        type = Task.Type.valueOf(t.type),
                    )
                }
            )
                .applyEvent(ev, fetcher)
        }
    }

    fun applyEvent(event: ClientFileEvent, fetcher: MutableFetcher): ClientFile {
        applyEventCount(event)
        applyTaskState(event)
        applyUpdateName(event)
        applyAcceptationDate(event)
        return this
    }

    private fun applyEventCount(event: ClientFileEvent) {
        eventCount += 1
    }

    private fun applyTaskState(event: ClientFileEvent) {
        when (event) {
            is CreationEvent -> {
                tasks
                    .forEach { it.state = Task.State.UNAVAILABLE }
                tasks
                    .documentTasks()
                    .forEach { it.state = Task.State.TODO }
            }

            is DocumentStartEvent -> {
                taskById(event.taskId)?.state = Task.State.IN_PROGRESS
            }

            is DocumentEvent -> {
                taskById(event.taskId)?.state = Task.State.DONE

                tasks
                    .signatureTasks()
                    .forEach { it.state = Task.State.TODO }
            }

            is SignatureEvent -> {
                taskById(event.taskId)?.state = Task.State.DONE
            }

            is ReopenEvent -> {
                val toReopened = event
                    .reopenedTasksId
                    .mapNotNull { taskById(it) }
                when {
                    toReopened.documentTasks().isNotEmpty() -> {
                        toReopened
                            .documentTasks()
                            .forEach { it.state = Task.State.TODO }
                    }

                    else -> toReopened.forEach { it.state = Task.State.TODO }

                }
            }

            is AcceptationEvent,
            is AutoAcceptationEvent -> {
                tasks
                    .forEach { it.state = Task.State.UNAVAILABLE }
            }


            is UpdateEvent -> {
            }
        }
    }

    private fun applyUpdateName(event: ClientFileEvent) {
        when (event) {
            is UpdateEvent -> {
                participant.name = event.newName
            }

            else -> {}
        }
    }

    private fun applyAcceptationDate(event: ClientFileEvent) {
        when (event) {
            is AcceptationEvent,
            is AutoAcceptationEvent -> {
                if (acceptationDate == null)
                    acceptationDate = event.dateTime
            }

            else -> {

            }
        }
    }

    fun taskById(id: String): Task? = tasks.firstOrNull { it.taskId == id }
    private fun List<Task>.documentTasks() = this.filter { it.type == Task.Type.DOCUMENT }
    private fun List<Task>.signatureTasks() = this.filter { it.type == Task.Type.SIGNATURE }
}
