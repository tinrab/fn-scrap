package com.flinect.scrap.message

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Tag("unit")
class MessageSerializerTest {
    @MessageTypes(
        AddTodoAction::class
    )
    private open class Action : Message()

    @MessageTypeName("test.add-todo")
    private data class AddTodoAction(
        val text: String,
        @MessageFieldIgnore
        val skipped: Boolean,
        @MessageFieldName("x")
        val y: Int
    ) : Action()

    private val actionSerializer = MessageSerializer.of(Action::class)

    @Test
    fun simple() {
        val todoAction = AddTodoAction("go to sleep", true, 42)
        assertEquals("test.add-todo", todoAction.type)

        val json = actionSerializer.encode(todoAction)
        assertEquals(
            "{\"type\":\"test.add-todo\",\"payload\":{\"text\":\"go to sleep\",\"x\":42}}",
            json
        )

        val parsed = actionSerializer.decode(json) as AddTodoAction
        assertEquals(todoAction.text, parsed.text)
        assertEquals(todoAction.y, parsed.y)
    }

    @Test
    fun empty() {
        val empty = EmptyMessage()
        val json = actionSerializer.encode(empty)
        assertEquals("{\"type\":\"builtin.empty\",\"payload\":{}}", json)
        val decoded = actionSerializer.decode<Message>(json)
        assert(decoded is EmptyMessage)
    }

    @Test
    fun extraField() {
        val parsed =
            actionSerializer.decode("{\"type\":\"test.add-todo\",\"payload\":{\"text\":\"go to sleep\",\"x\":42,\"y\":13}}") as AddTodoAction
        assertEquals("go to sleep", parsed.text)
        assertEquals(42, parsed.y)
    }

    @Test
    fun nonObject() {
        assertThrows<IllegalArgumentException> {
            actionSerializer.decode<Action>("42")
        }
    }

    @Test
    fun typeNotRegistered() {
        data class A(
            val x: Int
        ) : Action()
        assertThrows<IllegalArgumentException> {
            actionSerializer.encode(A(42))
        }
    }

    @Test
    fun noMessageTypeAnnotation() {
        data class A(
            val x: Int
        ) : Action()
        assertThrows<IllegalArgumentException> {
            actionSerializer.registerMessageType(A::class)
        }
    }

    @Test
    fun unknownType() {
        assertThrows<IllegalArgumentException> {
            actionSerializer.decode<Action>("{\"type\":\"A\"}")
        }
    }

    @Test
    fun missingPayload() {
        assertThrows<IllegalArgumentException> {
            actionSerializer.decode<Action>("{\"type\":\"test.add-todo\"}")
        }
    }
}
