import AcceptationEvent.Companion.createAcceptationEvent
import AutoAcceptationEvent.Companion.createAutoAcceptationEvent
import DocumentEvent.Companion.createDocumentEvent
import DocumentStartEvent.Companion.createDocumentStartEvent
import ReopenEvent.Companion.createReopenEvent
import SignatureEvent.Companion.createSignatureEvent
import UpdateEvent.Companion.createUpdateEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID.randomUUID

class ClientFileTest {

    @Test
    fun `EventCount start at 1`() {
        var clientFile = ClientFileFixtures.`given CNI and contract CLientFile`()
        clientFile.eventCount shouldBe 1L

        clientFile = clientFile.applyEvent {
            createDocumentStartEvent(operatorId = randomUUID(), taskId = "cni")
        }
        clientFile.eventCount shouldBe 2L
    }

    @Test
    fun `Creation to acceptance standard flow`() {
        var clientFile = ClientFileFixtures.`given CNI and contract CLientFile`()

        clientFile.cni().state shouldBe ClientFile.Task.State.TODO
        clientFile.contract().state shouldBe ClientFile.Task.State.UNAVAILABLE


        clientFile = clientFile.applyEvent {
            createDocumentStartEvent(operatorId = randomUUID(), taskId = "cni")
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.IN_PROGRESS
        clientFile.contract().state shouldBe ClientFile.Task.State.UNAVAILABLE


        clientFile = clientFile.applyEvent {
            createDocumentEvent(operatorId = randomUUID(), taskId = "cni", participantNameFound = "John")
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.DONE
        clientFile.contract().state shouldBe ClientFile.Task.State.TODO


        clientFile = clientFile.applyEvent {
            createSignatureEvent(taskId = "contract")
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.DONE
        clientFile.contract().state shouldBe ClientFile.Task.State.DONE


        clientFile = clientFile.applyEvent {
            createAcceptationEvent(operatorId = randomUUID())
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.UNAVAILABLE
        clientFile.contract().state shouldBe ClientFile.Task.State.UNAVAILABLE
    }

    @Test
    fun `Update Reopen tasks before Acceptation`() {
        var clientFile = ClientFileFixtures.`given CNI and contract CLientFile`()

        clientFile = clientFile.applyEvent {
            createDocumentStartEvent(operatorId = randomUUID(), taskId = "cni")
        }
        clientFile = clientFile.applyEvent {
            createDocumentEvent(operatorId = randomUUID(), taskId = "cni", participantNameFound = "Jane")
        }
        clientFile = clientFile.applyEvent {
            createSignatureEvent(taskId = "contract")
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.DONE
        clientFile.contract().state shouldBe ClientFile.Task.State.DONE
        clientFile.participant.name shouldBe "Jean"


        clientFile = clientFile.applyEvent {
            createUpdateEvent(operatorId = randomUUID(), newName = "Jane")
        }
        clientFile.participant.name shouldBe "Jane"


        clientFile = clientFile.applyEvent {
            createReopenEvent(operatorId = randomUUID(), reopenedTasks = listOf("cni", "contract"))
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.TODO
        clientFile.contract().state shouldBe ClientFile.Task.State.DONE


        clientFile = clientFile.applyEvent {
            createDocumentStartEvent(operatorId = randomUUID(), taskId = "cni")
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.IN_PROGRESS
        clientFile.contract().state shouldBe ClientFile.Task.State.DONE


        clientFile = clientFile.applyEvent {
            createDocumentEvent(operatorId = randomUUID(), taskId = "cni", participantNameFound = "Jean")
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.DONE
        clientFile.contract().state shouldBe ClientFile.Task.State.TODO


        clientFile = clientFile.applyEvent {
            createSignatureEvent(taskId = "contract")
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.DONE
        clientFile.contract().state shouldBe ClientFile.Task.State.DONE


        clientFile = clientFile.applyEvent {
            createReopenEvent(operatorId = randomUUID(), reopenedTasks = listOf("contract"))
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.DONE
        clientFile.contract().state shouldBe ClientFile.Task.State.TODO


        clientFile = clientFile.applyEvent {
            createSignatureEvent(taskId = "contract")
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.DONE
        clientFile.contract().state shouldBe ClientFile.Task.State.DONE


        clientFile = clientFile.applyEvent {
            createAcceptationEvent(operatorId = randomUUID())
        }
        clientFile.cni().state shouldBe ClientFile.Task.State.UNAVAILABLE
        clientFile.contract().state shouldBe ClientFile.Task.State.UNAVAILABLE
    }

    @Test
    fun `Update events`() {
        var clientFile = ClientFileFixtures.`given CNI and contract CLientFile`()
        clientFile.participant.name shouldBe "Jean"

        clientFile = clientFile.applyEvent {
            createUpdateEvent(operatorId = randomUUID(), newName = "Jane")
        }

        clientFile.participant.name shouldBe "Jane"
    }

    @Test
    fun `Acceptation events`() {
        var clientFile = ClientFileFixtures.`given CNI and contract CLientFile`()

        clientFile.acceptationDate = null

        val event = clientFile.createAcceptationEvent(operatorId = randomUUID())
        clientFile = clientFile.applyEvent { event }
        clientFile.acceptationDate shouldBe event.dateTime

        val event2 = clientFile.createAutoAcceptationEvent()
        clientFile = clientFile.applyEvent { event2 }
        clientFile.acceptationDate shouldBe event.dateTime
    }

    @Test
    fun `Legacy AcceptationDate`() {
        var clientFile = ClientFileFixtures.`given CNI and contract CLientFile`()

        val event = clientFile.createAcceptationEvent(operatorId = randomUUID())
        val event2 = clientFile.createAutoAcceptationEvent()

        val events = listOf(event, event2)

        fun getLegacyAcceptanceDate(): OffsetDateTime? {
            val manualAcceptationDate = events
                .filterIsInstance<AcceptationEvent>()
                .firstOrNull()
                ?.dateTime
            val autoAcceptationDate = events
                .filterIsInstance<AutoAcceptationEvent>()
                .firstOrNull()
                ?.dateTime

            return when {
                manualAcceptationDate != null && autoAcceptationDate != null -> {
                    if (manualAcceptationDate < autoAcceptationDate) manualAcceptationDate
                    else autoAcceptationDate
                }

                manualAcceptationDate != null -> manualAcceptationDate
                autoAcceptationDate != null -> autoAcceptationDate
                else -> null
            }
        }
        getLegacyAcceptanceDate() shouldBe event.dateTime
    }

    private fun ClientFile.cni(): ClientFile.Task = taskById("cni")!!
    private fun ClientFile.contract(): ClientFile.Task = taskById("contract")!!

    private fun ClientFile.applyEvent(block: ClientFile.() -> ClientFileEvent): ClientFile =
        this.applyEvent(block(this), MutableFetcher.Stub)
}
