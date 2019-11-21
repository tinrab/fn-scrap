package com.flinect.scrap.message

import com.flinect.scrap.message.broker.BrokerBuilder
import com.flinect.scrap.message.broker.BrokerConfig
import com.flinect.scrap.message.broker.ExchangeDurability
import com.flinect.scrap.message.broker.ExchangeType
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@Tag("integration")
class BrokerTest {
    @MessageTypes(
        AddTodoAction::class
    )
    private open class Action : Message()

    @MessageTypeName("ADD_TODO")
    private data class AddTodoAction(
        val text: String
    ) : Action()

    lateinit var config: Config
    lateinit var brokerConfig: BrokerConfig

    @BeforeEach
    fun setUp() {
        config = ConfigFactory.load()
        brokerConfig = BrokerConfig(
            host = config.getString("rabbitmq.host"),
            port = config.getInt("rabbitmq.port"),
            username = config.getString("rabbitmq.username"),
            password = config.getString("rabbitmq.password")
        )
    }

    @Test
    @Timeout(1)
    fun workQueues() = runBlocking<Unit> {
        val queueName = "work-queue"
        val broker = BrokerBuilder.of(
            brokerConfig,
            MessageSerializer.of(Action::class)
        ).addQueue(queueName).build()

        val channel = Channel<Message>()
        broker.processTask(queueName) {
            assert(it is AddTodoAction)
            channel.sendBlocking(it)
            return@processTask true
        }

        broker.scheduleTask(queueName, AddTodoAction("task"))

        channel.receive()
    }

    @Test
    @Timeout(1)
    fun fanout() = runBlocking {
        val exchangeName = "fanout-test"
        val broker = BrokerBuilder.of(
            brokerConfig,
            MessageSerializer.of(Action::class)
        ).addExchange(
            exchangeName,
            ExchangeType.FANOUT,
            ExchangeDurability.WEAK
        ).build()

        val channel = Channel<Message>()

        broker.subscribe(exchangeName) {
            channel.sendBlocking(it)
            return@subscribe true
        }

        for (i in 1..3) {
            broker.publish(
                exchangeName,
                AddTodoAction(i.toString())
            )
            delay(5)
        }

        var received = 0
        while (received < 3) {
            channel.receive()
            received++
        }
    }
}
