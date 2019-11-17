package com.flinect.scrap.message

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.BeforeTest

class BrokerTest {
    @MessageTypes(
        AddTodoAction::class
    )
    private interface Action : Message

    @MessageTypeName("ADD_TODO")
    private data class AddTodoAction(
        val text: String
    ) : Action

    lateinit var config: Config
    lateinit var brokerConfig: BrokerConfig

    @BeforeTest
    fun setUp() {
        config = ConfigFactory.load()
        brokerConfig = BrokerConfig(
            host = config.getString("rabbitmq.host"),
            port = config.getInt("rabbitmq.port")
        )
    }

    @Test(timeout = 1000)
    fun fanout() = runBlocking {
        val exchangeName = "fanout-test"
        val broker = BrokerBuilder.of(brokerConfig, Action::class)
            .addExchange(exchangeName, ExchangeType.FANOUT, ExchangeDurability.WEAK)
            .build()

        val channel = Channel<Action>()

        broker.subscribe(exchangeName) {
            runBlocking {
                channel.send(it)
            }
            return@subscribe true
        }

        for (i in 1..3) {
            broker.publish(exchangeName, AddTodoAction(i.toString()))
            delay(5)
        }

        var received = 0
        while (received < 3) {
            channel.receive()
            received++
        }
    }
}
